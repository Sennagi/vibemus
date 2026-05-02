package com.sennagi.vibemus.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.sennagi.vibemus.music.SongItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LocalPlaybackController(
    context: Context,
    private val onSongStarted: (SongItem) -> Unit
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var mediaController: MediaController? = null
    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
    private var progressJob: Job? = null
    private var pendingQueueRequest: PendingQueueRequest? = null
    private var hasPendingControllerInit = false

    var queue by mutableStateOf<List<SongItem>>(emptyList())
        private set

    var currentIndex by mutableIntStateOf(-1)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var currentPositionMs by mutableLongStateOf(0L)
        private set

    var durationMs by mutableLongStateOf(0L)
        private set

    var playMode by mutableIntStateOf(0) // 0: list repeat, 1: single repeat, 2: shuffle
        private set

    val currentSong: SongItem?
        get() = queue.getOrNull(currentIndex)

    val playlist: List<SongItem>
        get() = queue

    init {
        initializeControllerIfNeeded()
    }

    fun setQueue(
        newQueue: List<SongItem>,
        startIndex: Int,
        autoplay: Boolean
    ) {
        if (newQueue.isEmpty() || startIndex !in newQueue.indices) return
        queue = newQueue
        currentIndex = startIndex
        currentPositionMs = 0L
        durationMs = newQueue[startIndex].durationMs
        pendingQueueRequest = PendingQueueRequest(newQueue, startIndex, autoplay)
        isLoading = true
        initializeControllerIfNeeded()
        mediaController?.let { applyQueueRequest(it, pendingQueueRequest!!) }
    }

    fun playSong(
        song: SongItem,
        newQueue: List<SongItem>
    ) {
        val targetIndex = newQueue.indexOfFirst { it.id == song.id }
        if (targetIndex >= 0) {
            setQueue(newQueue, targetIndex, autoplay = true)
        }
    }

    fun togglePlayPause() {
        val controller = mediaController
        if (controller == null) {
            initializeControllerIfNeeded()
            pendingQueueRequest?.let { it.copy(autoplay = true) }?.also { pendingQueueRequest = it }
            return
        }

        if (controller.isPlaying) {
            controller.pause()
        } else if (controller.mediaItemCount == 0) {
            val song = currentSong ?: return
            setQueue(queue.ifEmpty { listOf(song) }, currentIndex.coerceAtLeast(0), autoplay = true)
        } else {
            controller.play()
        }
    }

    fun playNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
            controller.play()
        } else if (queue.isNotEmpty()) {
            controller.seekToDefaultPosition(0)
            controller.play()
        }
    }

    fun playPrevious() {
        val controller = mediaController ?: return
        if (currentPositionMs > 5_000L) {
            seekTo(0L)
            return
        }

        if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
            controller.play()
        } else {
            seekTo(0L)
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs.coerceAtLeast(0L))
        currentPositionMs = positionMs.coerceAtLeast(0L)
    }

    fun playAt(index: Int) {
        val controller = mediaController ?: return
        if (index !in queue.indices) return
        controller.seekToDefaultPosition(index)
        controller.play()
    }

    fun togglePlayMode() {
        playMode = (playMode + 1) % 3
        mediaController?.let(::applyPlayModeToController)
    }

    fun release() {
        progressJob?.cancel()
        mediaController?.release()
        mediaController = null
        controllerFuture?.cancel(true)
        controllerFuture = null
        scope.cancel()
    }

    private fun initializeControllerIfNeeded() {
        if (mediaController != null || controllerFuture != null || hasPendingControllerInit) return
        hasPendingControllerInit = true

        val sessionToken = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
        val future = MediaController.Builder(appContext, sessionToken).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                hasPendingControllerInit = false
                runCatching { future.get() }
                    .onSuccess { controller ->
                        mediaController = controller
                        controllerFuture = null
                        setupControllerListener(controller)
                        syncQueueFromController(controller)
                        syncStateFromController(controller)
                        pendingQueueRequest?.let {
                            applyQueueRequest(controller, it)
                        } ?: applyPlayModeToController(controller)
                    }
                    .onFailure {
                        controllerFuture = null
                    }
            },
            ContextCompat.getMainExecutor(appContext)
        )
    }

    private fun applyQueueRequest(controller: MediaController, request: PendingQueueRequest) {
        queue = request.queue
        currentIndex = request.startIndex
        currentPositionMs = 0L
        durationMs = request.queue.getOrNull(request.startIndex)?.durationMs ?: 0L
        isLoading = true

        controller.setMediaItems(
            request.queue.mapIndexed { index, song -> song.toMediaItem(index) },
            request.startIndex,
            0L
        )
        applyPlayModeToController(controller)
        controller.prepare()
        if (request.autoplay) {
            controller.playWhenReady = true
            controller.play()
        } else {
            controller.pause()
        }
        pendingQueueRequest = null
    }

    private fun setupControllerListener(controller: MediaController) {
        controller.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    this@LocalPlaybackController.isPlaying = isPlaying
                    if (isPlaying) {
                        currentSong?.let(onSongStarted)
                        startProgressUpdates()
                    } else {
                        stopProgressUpdates()
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    isLoading = playbackState == Player.STATE_BUFFERING
                    if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED) {
                        syncStateFromController(controller)
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    syncQueueFromController(controller)
                    syncStateFromController(controller)
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    syncPlayModeFromController(controller)
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    syncPlayModeFromController(controller)
                }
            }
        )
    }

    private fun syncQueueFromController(controller: MediaController) {
        if (controller.mediaItemCount == 0) return
        queue = List(controller.mediaItemCount) { index ->
            controller.getMediaItemAt(index)
        }.mapIndexedNotNull { index, item ->
            item.toSongItem(index)
        }
        currentIndex = controller.currentMediaItemIndex.takeIf { it in queue.indices } ?: currentIndex
    }

    private fun syncStateFromController(controller: MediaController) {
        currentIndex = controller.currentMediaItemIndex.takeIf { it in queue.indices } ?: currentIndex
        currentPositionMs = controller.currentPosition.coerceAtLeast(0L)
        durationMs = controller.duration
            .takeIf { it > 0L && it != C.TIME_UNSET }
            ?: currentSong?.durationMs
            ?: 0L
        isPlaying = controller.isPlaying
        isLoading = controller.playbackState == Player.STATE_BUFFERING
        syncPlayModeFromController(controller)
        if (isPlaying) {
            startProgressUpdates()
        }
    }

    private fun syncPlayModeFromController(controller: MediaController) {
        playMode = when {
            controller.shuffleModeEnabled -> 2
            controller.repeatMode == Player.REPEAT_MODE_ONE -> 1
            else -> 0
        }
    }

    private fun applyPlayModeToController(controller: MediaController) {
        when (playMode) {
            1 -> {
                controller.shuffleModeEnabled = false
                controller.repeatMode = Player.REPEAT_MODE_ONE
            }

            2 -> {
                controller.shuffleModeEnabled = true
                controller.repeatMode = Player.REPEAT_MODE_ALL
            }

            else -> {
                controller.shuffleModeEnabled = false
                controller.repeatMode = Player.REPEAT_MODE_ALL
            }
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    currentPositionMs = controller.currentPosition.coerceAtLeast(0L)
                    durationMs = controller.duration
                        .takeIf { it > 0L && it != C.TIME_UNSET }
                        ?: durationMs
                }
                delay(500L)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun SongItem.toMediaItem(index: Int): MediaItem {
        val extras = Bundle().apply {
            putLong(KEY_SONG_ID, id)
            putString(KEY_FOLDER, folder)
            putLong(KEY_DURATION_MS, durationMs)
            putLong(KEY_DATE_ADDED_SEC, dateAddedSec)
            putString(KEY_FORMAT_LABEL, formatLabel)
            putInt(KEY_QUEUE_INDEX, index)
        }
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(albumArtUri)
            .setIsPlayable(true)
            .setExtras(extras)
            .build()

        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(contentUri)
            .setMediaMetadata(metadata)
            .build()
    }

    private fun MediaItem.toSongItem(index: Int): SongItem? {
        val uri = localConfiguration?.uri ?: requestMetadata.mediaUri ?: return null
        val extras = mediaMetadata.extras
        return SongItem(
            id = extras?.getLong(KEY_SONG_ID) ?: mediaId.toLongOrNull() ?: index.toLong(),
            title = mediaMetadata.title?.toString().orEmpty().ifBlank { "\u672a\u77e5\u6b4c\u66f2" },
            artist = mediaMetadata.artist?.toString().orEmpty().ifBlank { "\u672a\u77e5\u827a\u672f\u5bb6" },
            folder = extras?.getString(KEY_FOLDER).orEmpty(),
            contentUri = uri,
            albumArtUri = mediaMetadata.artworkUri,
            durationMs = extras?.getLong(KEY_DURATION_MS) ?: 0L,
            dateAddedSec = extras?.getLong(KEY_DATE_ADDED_SEC) ?: 0L,
            formatLabel = extras?.getString(KEY_FORMAT_LABEL).orEmpty()
        )
    }

    private data class PendingQueueRequest(
        val queue: List<SongItem>,
        val startIndex: Int,
        val autoplay: Boolean
    )

    private companion object {
        const val KEY_SONG_ID = "song_id"
        const val KEY_FOLDER = "folder"
        const val KEY_DURATION_MS = "duration_ms"
        const val KEY_DATE_ADDED_SEC = "date_added_sec"
        const val KEY_FORMAT_LABEL = "format_label"
        const val KEY_QUEUE_INDEX = "queue_index"
    }
}
