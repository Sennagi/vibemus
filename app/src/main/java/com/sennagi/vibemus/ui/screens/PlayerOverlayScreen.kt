package com.sennagi.vibemus.ui.screens

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.sennagi.senui.components.ActionSheet
import com.sennagi.senui.components.ActionSheetProgressButton
import com.sennagi.senui.components.CompactSwitch
import com.sennagi.senui.components.SheetBounceContainer
import com.sennagi.senui.components.bounceOverscroll
import com.sennagi.vibemus.music.EmbeddedLyricsReader
import com.sennagi.vibemus.music.LyricsContent
import com.sennagi.vibemus.music.SongItem
import com.sennagi.vibemus.music.TimedLyricLine
import com.sennagi.vibemus.ui.components.BounceIconButton
import com.sennagi.vibemus.ui.components.InteractiveProgressBar
import com.sennagi.vibemus.ui.theme.FavoriteRed
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToInt



@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerOverlayScreen(
    song: SongItem?,
    isPlaying: Boolean,
    isLoading: Boolean,
    isFavorite: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    playlist: List<SongItem>,
    currentSongIndex: Int,
    playMode: Int,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlayPrevious: () -> Unit,
    onTogglePlayback: () -> Unit,
    onPlayNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onPlaySongFromPlaylist: (SongItem) -> Unit,
    onTogglePlayMode: () -> Unit,
    sleepTimerRemainingMs: Long,
    sleepTimerDurationMs: Long,
    sleepTimerHistory: List<Long>,
    sleepTimerExtendToSongEnd: Boolean,
    sleepTimerAwaitingSongEnd: Boolean,
    onSetSleepTimer: (Long, Boolean) -> Unit,
    onClearSleepTimer: () -> Unit
) {
    if (song == null) return
    val context = LocalContext.current
    val view = LocalView.current

    // Ensure status bar icons switch to "light" (white icons) while the full-screen player is visible.
    val activity = context as? Activity
    val window = activity?.window
    if (window != null && !view.isInEditMode) {
        val controller = WindowCompat.getInsetsController(window, view)
        DisposableEffect(window, view) {
            val prevLightStatusBars = controller.isAppearanceLightStatusBars
            val prevLightNavBars = controller.isAppearanceLightNavigationBars
            val prevStatusBarColor = window.statusBarColor
            val prevNavBarColor = window.navigationBarColor
            onDispose {
                window.statusBarColor = prevStatusBarColor
                window.navigationBarColor = prevNavBarColor
                controller.isAppearanceLightStatusBars = prevLightStatusBars
                controller.isAppearanceLightNavigationBars = prevLightNavBars
            }
        }
        SideEffect {
            window.statusBarColor = 0
            window.navigationBarColor = 0
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    val configuration = LocalConfiguration.current
    val isActualLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useSplitLayout = isActualLandscape
    var showPlaylist by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var lyricsLoading by remember(song.contentUri) { mutableStateOf(true) }
    var lyricsContent by remember(song.contentUri) { mutableStateOf<LyricsContent?>(null) }
    val density = LocalDensity.current
    var stableStatusBarTopPadding by remember { mutableStateOf(0.dp) }

    BackHandler(enabled = !showPlaylist) {
        onDismiss()
    }

    DisposableEffect(view, isActualLandscape, density) {
        if (isActualLandscape) {
            onDispose {}
        } else {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
                val statusTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                val cutoutTop = insets.getInsets(WindowInsetsCompat.Type.displayCutout()).top
                val topPx = if (statusTop > cutoutTop) statusTop else cutoutTop
                if (topPx > 0) {
                    val topDp = with(density) { topPx.toDp() }
                    if (topDp > stableStatusBarTopPadding) stableStatusBarTopPadding = topDp
                }
                insets
            }
            androidx.core.view.ViewCompat.requestApplyInsets(view)
            onDispose {
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(view, null)
            }
        }
    }

    // Gradient State - 娴犲簼绗撴潏鎴濇禈閹绘劕褰囨０婊嗗
    var gradientTop by remember { mutableStateOf(Color.DarkGray) }
    var gradientBottom by remember { mutableStateOf(Color.Black) }
    val animatedGradientTop by animateColorAsState(targetValue = gradientTop, animationSpec = tween(260), label = "animatedGradientTop")
    val animatedGradientBottom by animateColorAsState(targetValue = gradientBottom, animationSpec = tween(260), label = "animatedGradientBottom")

    // Extract colors from album art using Palette
    LaunchedEffect(song?.albumArtUri) {
        val uri = song?.albumArtUri ?: return@LaunchedEffect
        runCatching {
            val request = ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false)
                .size(512)
                .build()

            val bitmap = withContext(Dispatchers.IO) {
                val result = context.imageLoader.execute(request)
                val drawable = (result as? coil.request.SuccessResult)?.drawable as? android.graphics.drawable.BitmapDrawable
                drawable?.bitmap
            } ?: return@runCatching

            val (vibrant, darkVibrant) = withContext(Dispatchers.Default) {
                val palette = androidx.palette.graphics.Palette.from(bitmap).generate()
                palette.getVibrantColor(android.graphics.Color.DKGRAY) to
                    palette.getDarkVibrantColor(android.graphics.Color.BLACK)
            }

            gradientTop = Color(vibrant)
            gradientBottom = Color(darkVibrant)
        }.onFailure {
            it.printStackTrace()
        }
    }

    LaunchedEffect(song.contentUri) {
        lyricsLoading = true
        lyricsContent = EmbeddedLyricsReader.load(context, song)
        lyricsLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(animatedGradientTop, animatedGradientBottom)))
    ) {
        // Blurred Background Image
        Crossfade(targetState = song.albumArtUri, animationSpec = tween(260), label = "backgroundCrossfade") { coverUrl ->
            if (coverUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(coverUrl)
                        .crossfade(false)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(50.dp) else Modifier.alpha(0.3f))
                        .alpha(0.5f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isActualLandscape) Modifier else Modifier.padding(top = stableStatusBarTopPadding))
                .navigationBarsPadding()
        ) {
            // Top Bar
            Spacer(modifier = Modifier.height(12.dp))

            if (useSplitLayout) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        val controlsWidth = minOf(maxWidth, 460.dp)
                        PlayerControlsView(
                            song = song,
                            isPlaying = isPlaying,
                            isLoading = isLoading,
                            isFavorite = isFavorite,
                            currentPositionMs = currentPositionMs,
                            durationMs = durationMs,
                            playMode = playMode,
                            onToggleFavorite = onToggleFavorite,
                            onPlayPrevious = onPlayPrevious,
                            onTogglePlayback = onTogglePlayback,
                            onPlayNext = onPlayNext,
                            onSeek = onSeek,
                            onTogglePlayMode = onTogglePlayMode,
                            onShowPlaylist = { showPlaylist = true },
                            sleepTimerRemainingMs = sleepTimerRemainingMs,
                            onShowSleepTimer = { showSleepTimerSheet = true },
                            isSplitLayout = true,
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(controlsWidth)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        LyricsPanel(
                            song = song,
                            lyricsContent = lyricsContent,
                            isLoading = lyricsLoading,
                            currentPositionMs = currentPositionMs,
                            durationMs = durationMs,
                            isSplitLayout = true,
                            isPlaying = isPlaying,
                            onTogglePlayback = onTogglePlayback,
                            onSeek = onSeek,
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth()
                                .padding(start = 4.dp, end = 24.dp, top = 12.dp, bottom = 12.dp)
                        )
                    }
                }
            } else {
                val pagerState = rememberPagerState(pageCount = { 2 })
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    if (page == 0) {
                        PlayerControlsView(
                            song = song,
                            isPlaying = isPlaying,
                            isLoading = isLoading,
                            isFavorite = isFavorite,
                            currentPositionMs = currentPositionMs,
                            durationMs = durationMs,
                            playMode = playMode,
                            onToggleFavorite = onToggleFavorite,
                            onPlayPrevious = onPlayPrevious,
                            onTogglePlayback = onTogglePlayback,
                            onPlayNext = onPlayNext,
                            onSeek = onSeek,
                            onTogglePlayMode = onTogglePlayMode,
                            onShowPlaylist = { showPlaylist = true },
                            sleepTimerRemainingMs = sleepTimerRemainingMs,
                            onShowSleepTimer = { showSleepTimerSheet = true }
                        )
                    } else {
                        LyricsPanel(
                            song = song,
                            lyricsContent = lyricsContent,
                            isLoading = lyricsLoading,
                            currentPositionMs = currentPositionMs,
                            durationMs = durationMs,
                            isSplitLayout = false,
                            isPlaying = isPlaying,
                            onTogglePlayback = onTogglePlayback,
                            onSeek = onSeek,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        if (showSleepTimerSheet) {
            SleepTimerActionSheet(
                remainingMs = sleepTimerRemainingMs,
                timerDurationMs = sleepTimerDurationMs,
                historyDurations = sleepTimerHistory,
                extendToSongEnd = sleepTimerExtendToSongEnd,
                awaitingSongEnd = sleepTimerAwaitingSongEnd,
                onDismiss = { showSleepTimerSheet = false },
                onSetTimer = onSetSleepTimer,
                onClearTimer = onClearSleepTimer
            )
        }

        // Playlist Sheet
        if (showPlaylist) {
            val playlistListState = rememberLazyListState()
            val playlistCoroutineScope = rememberCoroutineScope()
            val playlistSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val playlistSheetCoroutineScope = rememberCoroutineScope()

            fun animateAndDismiss(action: () -> Unit) {
                playlistSheetCoroutineScope.launch {
                    playlistSheetState.hide()
                }.invokeOnCompletion {
                    if (!playlistSheetState.isVisible) action()
                }
            }

            LaunchedEffect(showPlaylist, currentSongIndex) {
                if (showPlaylist && currentSongIndex >= 0) {
                    playlistListState.scrollToItem((currentSongIndex - 1).coerceAtLeast(0))
                }
            }

            ModalBottomSheet(
                onDismissRequest = { animateAndDismiss { showPlaylist = false } },
                sheetState = playlistSheetState,
                containerColor = MaterialTheme.colorScheme.background,
                scrimColor = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                dragHandle = null
            ) {
                val dialogView = LocalView.current

                DisposableEffect(dialogView) {
                    (dialogView.parent as? DialogWindowProvider)?.window?.let { window ->
                        WindowCompat.setDecorFitsSystemWindows(window, false)
                        window.setLayout(
                            android.view.WindowManager.LayoutParams.MATCH_PARENT,
                            android.view.WindowManager.LayoutParams.MATCH_PARENT
                        )
                        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                        window.setBackgroundDrawable(
                            android.graphics.drawable.ColorDrawable(0)
                        )
                        window.navigationBarColor = 0
                        window.statusBarColor = 0
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            window.isNavigationBarContrastEnforced = false
                            window.isStatusBarContrastEnforced = false
                        }

                        window.decorView.systemUiVisibility =
                            window.decorView.systemUiVisibility or
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                        }

                        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
                            val types = WindowInsetsCompat.Type.navigationBars() or
                                WindowInsetsCompat.Type.systemGestures() or
                                WindowInsetsCompat.Type.mandatorySystemGestures()
                            val adjusted = WindowInsetsCompat.Builder(insets)
                                .setInsets(types, androidx.core.graphics.Insets.NONE)
                                .build()
                            androidx.core.view.ViewCompat.onApplyWindowInsets(v, adjusted)
                            adjusted
                        }
                        androidx.core.view.ViewCompat.requestApplyInsets(window.decorView)

                        val bottomSheet =
                            window.decorView.findViewById<View>(android.R.id.content)
                        bottomSheet?.let { sheet ->
                            sheet.setFitsSystemWindows(false)
                            (sheet.parent as? View)?.setFitsSystemWindows(false)
                            sheet.setBackgroundColor(0)

                            val lp = sheet.layoutParams
                            if (lp != null) {
                                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                                sheet.layoutParams = lp
                            }
                        }
                    }
                    onDispose { }
                }

                val navBarBottomPadding = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }

                val collapsedSheetHeight = (configuration.screenHeightDp.dp * 0.52f).coerceAtLeast(320.dp)
                val sheetHeight by animateDpAsState(
                    targetValue = collapsedSheetHeight,
                    animationSpec = tween(durationMillis = 260),
                    label = "playlistSheetHeight"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(sheetHeight + navBarBottomPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 18.dp, bottom = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            onClick = {
                                animateAndDismiss { showPlaylist = false }
                            },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .size(44.dp)
                                .align(Alignment.CenterStart)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "\u64ad\u653e\u5217\u8868",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\u66f2\u76ee\u6570\u91cf ${playlist.size} \u9996",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Playlist items
                    SheetBounceContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        LazyColumn(
                            state = playlistListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 0.dp,
                                bottom = navBarBottomPadding + 8.dp
                            )
                        ) {
                            itemsIndexed(playlist) { _, item ->
                                val isCurrent = item.id == song.id

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor =
                                        if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 6.dp)
                                        .height(70.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable { onPlaySongFromPlaylist(item) }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                            ) {
                                                AsyncImage(
                                                    model = item.albumArtUri,
                                                    contentDescription = item.title,
                                                    modifier = Modifier.matchParentSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(start = 16.dp)
                                            ) {
                                                Text(
                                                    text = item.title,
                                                    color =
                                                    if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                                                    fontSize = 16.sp,
                                                    fontWeight =
                                                    if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = item.artist,
                                                    color =
                                                    if (isCurrent) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 14.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Box(modifier = Modifier.size(36.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerControlsView(
    song: SongItem,
    isPlaying: Boolean,
    isLoading: Boolean,
    isFavorite: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    playMode: Int,
    onToggleFavorite: () -> Unit,
    onPlayPrevious: () -> Unit,
    onTogglePlayback: () -> Unit,
    onPlayNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onTogglePlayMode: () -> Unit,
    onShowPlaylist: () -> Unit,
    sleepTimerRemainingMs: Long,
    onShowSleepTimer: () -> Unit,
    isSplitLayout: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isCompact = isSplitLayout
    val controlsHorizontalPadding = if (isCompact) 36.dp else 24.dp
    val coverHorizontalPadding = if (isCompact) 36.dp else 24.dp
    val coverCornerRadius = if (isCompact) 16.dp else 20.dp
    val infoVerticalPadding = if (isCompact) 12.dp else 16.dp
    val infoToControlsSpacer = if (isCompact) 14.dp else 24.dp
    val prevNextButtonSize = if (isCompact) 42.dp else 56.dp
    val prevNextIconSize = if (isCompact) 34.dp else 44.dp
    val playButtonSize = if (isCompact) 74.dp else 90.dp
    val playIconSize = if (isCompact) 54.dp else 66.dp
    val bottomIconButtonSize = if (isCompact) 40.dp else 44.dp
    val bottomIconSize = if (isCompact) 20.dp else 22.dp
    val titleTextStyle = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall
    val artistTextStyle = if (isCompact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
    val timeFontSize = if (isCompact) 11.sp else 12.sp

    val progress = if (durationMs > 0) currentPositionMs.toFloat() / durationMs else 0f

    val controlsContent: @Composable (Modifier) -> Unit = { controlsModifier ->
        Column(modifier = controlsModifier) {
            // Song Info & Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = titleTextStyle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee()
                    )
                    Text(
                        text = song.artist,
                        style = artistTextStyle,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                BounceIconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) FavoriteRed else Color.White,
                        modifier = Modifier.size(if (isCompact) 22.dp else 28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(infoToControlsSpacer))

            // Interactive Progress Bar
            InteractiveProgressBar(
                progress = progress,
                duration = durationMs,
                onSeek = onSeek
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(currentPositionMs), color = Color.White.copy(alpha = 0.7f), fontSize = timeFontSize)
                Text(formatTime(durationMs), color = Color.White.copy(alpha = 0.7f), fontSize = timeFontSize)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Play Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BounceIconButton(onClick = onPlayPrevious, buttonSize = prevNextButtonSize) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Prev",
                        tint = Color.White,
                        modifier = Modifier.size(prevNextIconSize)
                    )
                }

                BounceIconButton(
                    onClick = onTogglePlayback,
                    buttonSize = playButtonSize
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(playIconSize)
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier
                                .size(playIconSize)
                                .scale(if (isPlaying) 1.02f else 1.08f)
                        )
                    }
                }

                BounceIconButton(onClick = onPlayNext, buttonSize = prevNextButtonSize) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(prevNextIconSize)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. 妞ゅ搫绨敍鍫熸尡閺€鐐佸蹇ョ礆Button
                val modeIcon = when (playMode) {
                    0 -> Icons.Rounded.Repeat
                    1 -> Icons.Rounded.RepeatOne
                    2 -> Icons.Rounded.Shuffle
                    else -> Icons.Rounded.Repeat
                }
                IconButton(
                    onClick = onTogglePlayMode,
                    modifier = Modifier.size(bottomIconButtonSize)
                ) {
                    Icon(
                        imageVector = modeIcon,
                        contentDescription = "播放模式",
                        tint = Color.White,
                        modifier = Modifier.size(bottomIconSize)
                    )
                }

                // 2. 瀹氭椂鍏抽棴 Button
                IconButton(
                    onClick = onShowSleepTimer,
                    modifier = Modifier.size(bottomIconButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = "定时关闭",
                        tint = if (sleepTimerRemainingMs > 0L) MaterialTheme.colorScheme.primary else Color.White,
                        modifier = Modifier.size(bottomIconSize)
                    )
                }
                // 3. 閹绢厽鏂侀崚妤勩€?Button
                IconButton(
                    onClick = onShowPlaylist,
                    modifier = Modifier.size(bottomIconButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = "播放列表",
                        tint = Color.White,
                        modifier = Modifier.size(bottomIconSize)
                    )
                }

                // 4. 鏇村 Button
                IconButton(
                    onClick = { /* 閺囨潙顦块崝鐔诲厴瀵板懎鐤勯悳?*/ },
                    modifier = Modifier.size(bottomIconButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "更多",
                        tint = Color.White,
                        modifier = Modifier.size(bottomIconSize)
                    )
                }
            }
        }
    }

    if (isCompact) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val contentWidth = minOf((maxWidth - coverHorizontalPadding * 2).coerceAtLeast(0.dp), 340.dp)
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = coverHorizontalPadding),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Crossfade(targetState = song.albumArtUri, animationSpec = tween(260), label = "coverCrossfade") { coverUrl ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(coverUrl)
                                .crossfade(true)
                                .size(1100)
                                .build(),
                            contentDescription = "Cover",
                            modifier = Modifier
                                .padding(bottom = 24.dp)
                                .size(contentWidth)
                                .clip(RoundedCornerShape(coverCornerRadius))
                                .background(Color.Gray.copy(alpha = 0.3f)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = controlsHorizontalPadding, end = controlsHorizontalPadding, bottom = 16.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    controlsContent(
                        Modifier
                            .width(contentWidth)
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Crossfade(targetState = song.albumArtUri, animationSpec = tween(260), label = "coverCrossfade") { coverUrl ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(coverUrl)
                            .crossfade(true)
                            .size(1000)
                            .build(),
                        contentDescription = "Cover",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = coverHorizontalPadding)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(coverCornerRadius))
                            .background(Color.Gray.copy(alpha = 0.3f)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            controlsContent(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = controlsHorizontalPadding, vertical = infoVerticalPadding)
            )
        }
    }
}

@Composable
private fun LyricsPanel(
    song: SongItem,
    lyricsContent: LyricsContent?,
    isLoading: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isSplitLayout: Boolean,
    isPlaying: Boolean,
    onTogglePlayback: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val panelShape = if (isSplitLayout) RoundedCornerShape(28.dp) else RoundedCornerShape(0.dp)
    Surface(
        modifier = modifier,
        shape = panelShape,
        color = Color.Transparent
    ) {
        when {
            isLoading -> LyricsMessage(
                title = "\u6b63\u5728\u8bfb\u53d6\u6b4c\u8bcd",
                subtitle = "\u6b63\u5728\u5c1d\u8bd5\u8bfb\u53d6\u6b4c\u66f2\u5185\u7f6e\u6b4c\u8bcd",
                isSplitLayout = isSplitLayout
            )

            lyricsContent == null -> LyricsMessage(
                title = "\u672a\u68c0\u6d4b\u5230\u6b4c\u8bcd",
                subtitle = null,
                isSplitLayout = isSplitLayout
            )

            lyricsContent.hasTiming -> TimedLyricsView(
                song = song,
                lyrics = lyricsContent.timedLines,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isSplitLayout = isSplitLayout,
                isPlaying = isPlaying,
                onTogglePlayback = onTogglePlayback,
                onSeek = onSeek
            )

            else -> PlainLyricsView(
                song = song,
                lyrics = lyricsContent.plainLines,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isSplitLayout = isSplitLayout,
                isPlaying = isPlaying,
                onTogglePlayback = onTogglePlayback,
                onSeek = onSeek
            )
        }
    }
}

@Composable
private fun LyricsMessage(
    title: String,
    subtitle: String?,
    isSplitLayout: Boolean
) {
    val horizontalPadding = if (isSplitLayout) 28.dp else 24.dp
    val resolvedTitle = title
    val resolvedSubtitle = subtitle
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = resolvedTitle,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = if (isSplitLayout) 24.sp else 26.sp,
                textAlign = TextAlign.Center
            )
            if (!resolvedSubtitle.isNullOrBlank()) {
                Text(
                    text = resolvedSubtitle,
                    color = Color.White.copy(alpha = 0.68f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimedLyricsView(
    song: SongItem,
    lyrics: List<TimedLyricLine>,
    currentPositionMs: Long,
    durationMs: Long,
    isSplitLayout: Boolean,
    isPlaying: Boolean,
    onTogglePlayback: () -> Unit,
    onSeek: (Long) -> Unit
) {
    val listState = rememberLazyListState()
    val isDragged by listState.interactionSource.collectIsDraggedAsState()
    val isUserScrolling = remember { mutableStateOf(false) }
    val activeIndex = remember(lyrics, currentPositionMs) {
        lyrics.indexOfLast { it.timeMs <= currentPositionMs }
    }

    LaunchedEffect(isDragged) {
        if (isDragged) {
            isUserScrolling.value = true
        } else if (isUserScrolling.value) {
            delay(1000)
            isUserScrolling.value = false
        }
    }

    LaunchedEffect(activeIndex, isUserScrolling.value) {
        if (activeIndex < 0 || isUserScrolling.value || lyrics.isEmpty()) return@LaunchedEffect

        val visibleInfo = listState.layoutInfo.visibleItemsInfo.find { it.index == activeIndex }
        val layoutInfo = listState.layoutInfo
        val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
        val targetCenter = (viewportHeight * 0.15f).roundToInt()

        if (visibleInfo != null) {
            val itemCenter = visibleInfo.offset + (visibleInfo.size / 2)
            val delta = itemCenter - targetCenter
            if (kotlin.math.abs(delta) > 10) {
                listState.animateScrollBy(
                    value = delta.toFloat(),
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                )
            }
        } else {
            listState.animateScrollToItem(
                index = activeIndex,
                scrollOffset = -targetCenter
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (!isSplitLayout) {
            LyricsHeader(
                song = song,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isPlaying = isPlaying,
                onTogglePlayback = onTogglePlayback,
                onSeek = onSeek
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .bounceOverscroll()
            ) {
                val boxHeight = maxHeight
                val topPadding = boxHeight * 0.15f
                val bottomPadding = boxHeight * 0.5f
                val edgeFadeBrush = remember {
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.08f to Color.Black,
                            0.92f to Color.Black,
                            1f to Color.Transparent
                        )
                    )
                }

                CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .fadingEdge(edgeFadeBrush),
                        contentPadding = PaddingValues(top = topPadding, bottom = bottomPadding),
                        horizontalAlignment = Alignment.Start
                    ) {
                        itemsIndexed(
                            items = lyrics,
                            key = { index, line -> "${line.timeMs}-$index" }
                        ) { index, line ->
                            val isCurrentLine = index == activeIndex
                            val distanceFromCurrent = (index - activeIndex).absoluteValue
                            val targetBlur = if (isCurrentLine || isUserScrolling.value) {
                                0.dp
                            } else {
                                (distanceFromCurrent * 1.5).coerceAtMost(6.0).dp
                            }
                            val targetAlpha = if (isCurrentLine) 1f else 0.5f
                            val targetScale = if (isCurrentLine) 1.1f else 1f

                            val alpha by animateFloatAsState(
                                targetValue = targetAlpha,
                                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                                label = "lyricsAlpha"
                            )
                            val blurRadius by animateDpAsState(
                                targetValue = targetBlur,
                                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                                label = "lyricsBlur"
                            )
                            val scale by animateFloatAsState(
                                targetValue = targetScale,
                                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                                label = "lyricsScale"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0.1.dp) {
                                            Modifier.blur(blurRadius)
                                        } else {
                                            Modifier
                                        }
                                    )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 24.dp, end = 48.dp, top = 16.dp, bottom = 16.dp)
                                        .graphicsLayer {
                                            this.alpha = alpha
                                            this.scaleX = scale
                                            this.scaleY = scale
                                            this.transformOrigin = TransformOrigin(0f, 0.5f)
                                        }
                                ) {
                                    Text(
                                        text = line.text,
                                        color = Color.White,
                                        textAlign = TextAlign.Start,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 28.sp
                                        ),
                                        lineHeight = 40.sp
                                    )
                                    if (!line.translation.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = line.translation,
                                            color = Color.White.copy(alpha = 0.55f),
                                            textAlign = TextAlign.Start,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 18.sp
                                            ),
                                            lineHeight = 26.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlainLyricsView(
    song: SongItem,
    lyrics: List<String>,
    currentPositionMs: Long,
    durationMs: Long,
    isSplitLayout: Boolean,
    isPlaying: Boolean,
    onTogglePlayback: () -> Unit,
    onSeek: (Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (!isSplitLayout) {
            LyricsHeader(
                song = song,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isPlaying = isPlaying,
                onTogglePlayback = onTogglePlayback,
                onSeek = onSeek
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        SheetBounceContainer(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 96.dp, bottom = 180.dp),
                horizontalAlignment = Alignment.Start
            ) {
                itemsIndexed(lyrics) { _, line ->
                    Text(
                        text = line,
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 40.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 48.dp, top = 16.dp, bottom = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LyricsHeader(
    song: SongItem,
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onTogglePlayback: () -> Unit,
    onSeek: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.albumArtUri,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Gray.copy(alpha = 0.3f)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onTogglePlayback,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = "Play/Pause",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        InteractiveProgressBar(
            progress = if (durationMs > 0) currentPositionMs.toFloat() / durationMs else 0f,
            duration = durationMs,
            onSeek = onSeek
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(currentPositionMs), color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            Text(formatTime(durationMs), color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
        }
    }
}

private fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }

@Composable
private fun SleepTimerActionSheet(
    remainingMs: Long,
    timerDurationMs: Long,
    historyDurations: List<Long>,
    extendToSongEnd: Boolean,
    awaitingSongEnd: Boolean,
    onDismiss: () -> Unit,
    onSetTimer: (Long, Boolean) -> Unit,
    onClearTimer: () -> Unit
) {
    // 浣跨敤 remember 鑰屼笉鏄?rememberSaveable锛岀‘淇濇瘡娆℃墦寮€閮芥槸榛樿鍊?
    var selectedHours by remember {
        mutableIntStateOf(0)
    }
    var selectedMinutes by remember {
        mutableIntStateOf(0)
    }
    var selectedSeconds by remember {
        mutableIntStateOf(0)
    }
    var extendToSongEndEnabled by remember {
        mutableStateOf(extendToSongEnd)
    }

    val selectedDurationMsRaw = remember(selectedHours, selectedMinutes, selectedSeconds) {
        (
            selectedHours.toLong() * 3_600_000L +
                selectedMinutes.toLong() * 60_000L +
                selectedSeconds.toLong() * 1_000L
            )
    }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val isTimerRunning = remainingMs > 0L || awaitingSongEnd
    var wasTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning) {
        if (wasTimerRunning && !isTimerRunning) {
            selectedHours = 0
            selectedMinutes = 0
            selectedSeconds = 0
        }
        wasTimerRunning = isTimerRunning
    }

    // 鍊掕鏃惰繍琛屾椂锛屾樉绀哄墿浣欐椂闂达紱鍚﹀垯鏄剧ず閫夋嫨鐨勬椂闂?
    val displayHours = if (isTimerRunning && !awaitingSongEnd) {
        ((remainingMs / 3_600_000L) % 24L).toInt()
    } else {
        selectedHours
    }
    val displayMinutes = if (isTimerRunning && !awaitingSongEnd) {
        ((remainingMs % 3_600_000L) / 60_000L).toInt()
    } else {
        selectedMinutes
    }
    val displaySeconds = if (isTimerRunning && !awaitingSongEnd) {
        ((remainingMs % 60_000L) / 1_000L).toInt()
    } else {
        selectedSeconds
    }
    val timerProgressAnim = remember { Animatable(0f) }
    LaunchedEffect(isTimerRunning, awaitingSongEnd, timerDurationMs) {
        when {
            !isTimerRunning -> {
                timerProgressAnim.snapTo(0f)
            }
            awaitingSongEnd -> {
                timerProgressAnim.snapTo(1f)
            }
            else -> {
                val durationMs = timerDurationMs.coerceAtLeast(1_000L)
                val remainingForRunMs = remainingMs.coerceIn(0L, durationMs)
                val startProgress =
                    (1f - remainingForRunMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                timerProgressAnim.stop()
                timerProgressAnim.snapTo(startProgress)

                if (remainingForRunMs > 0L && startProgress < 1f) {
                    timerProgressAnim.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = remainingForRunMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                            easing = LinearEasing
                        )
                    )
                } else {
                    timerProgressAnim.snapTo(1f)
                }
            }
        }
    }
    val timerProgress = timerProgressAnim.value

    fun applyDuration(durationMs: Long) {
        val safeDurationMs = durationMs.coerceAtLeast(1_000L)
        selectedHours = ((safeDurationMs / 3_600_000L) % 24L).toInt()
        selectedMinutes = ((safeDurationMs % 3_600_000L) / 60_000L).toInt()
        selectedSeconds = ((safeDurationMs % 60_000L) / 1_000L).toInt()
    }

    ActionSheet(
        title = "定时关闭",
        onDismiss = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            // 鍐呭鍖哄煙锛堝彲婊氬姩锛屽甫瓒婄晫鍥炲脊锛?
            SheetBounceContainer {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 140.dp), // 涓哄簳閮ㄦ寜閽暀鍑虹┖闂?
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            when (page) {
                                0 -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            WheelNumberPickerColumn(
                                                valueRange = 0..23,
                                                selectedValue = displayHours,
                                                onValueChange = { selectedHours = it },
                                                isActive = isTimerRunning && !awaitingSongEnd
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .height(64.dp)
                                                    .padding(horizontal = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = ":",
                                                    color = if (isTimerRunning && !awaitingSongEnd) {
                                                        MaterialTheme.colorScheme.primary
                                                    } else {
                                                        MaterialTheme.colorScheme.onBackground
                                                    },
                                                    fontSize = 38.sp,
                                                    fontWeight = FontWeight.Light,
                                                    lineHeight = 38.sp
                                                )
                                            }
                                            WheelNumberPickerColumn(
                                                valueRange = 0..59,
                                                selectedValue = displayMinutes,
                                                onValueChange = { selectedMinutes = it },
                                                isActive = isTimerRunning && !awaitingSongEnd
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .height(64.dp)
                                                    .padding(horizontal = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = ":",
                                                    color = if (isTimerRunning && !awaitingSongEnd) {
                                                        MaterialTheme.colorScheme.primary
                                                    } else {
                                                        MaterialTheme.colorScheme.onBackground
                                                    },
                                                    fontSize = 38.sp,
                                                    fontWeight = FontWeight.Light,
                                                    lineHeight = 38.sp
                                                )
                                            }
                                            WheelNumberPickerColumn(
                                                valueRange = 0..59,
                                                selectedValue = displaySeconds,
                                                onValueChange = { selectedSeconds = it },
                                                isActive = isTimerRunning && !awaitingSongEnd
                                            )
                                        }
                                    }
                                }

                                else -> {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(bottom = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(start = 6.dp, end = 6.dp, bottom = 4.dp)
                                    ) {
                                        item {
                                            Surface(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(24.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "播完整首后再关闭",
                                                            color = MaterialTheme.colorScheme.onBackground,
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "倒计时结束后继续播放当前歌曲。",
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontSize = 13.sp,
                                                            lineHeight = 20.sp
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(14.dp))
                                                    CompactSwitch(
                                                        checked = extendToSongEndEnabled,
                                                        onCheckedChange = { extendToSongEndEnabled = it },
                                                        width = 60.dp,
                                                        height = 34.dp
                                                    )
                                                }
                                            }
                                        }

                                        itemsIndexed(historyDurations) { _, durationMs ->
                                            val selected = durationMs == selectedDurationMsRaw
                                            Surface(
                                                onClick = { applyDuration(durationMs) },
                                                shape = RoundedCornerShape(24.dp),
                                                color = if (selected) {
                                                    MaterialTheme.colorScheme.surface
                                                } else {
                                                    MaterialTheme.colorScheme.background
                                                },
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (selected) {
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                                    } else {
                                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                                    }
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 18.dp, vertical = 16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = formatDurationClock(durationMs),
                                                        color = MaterialTheme.colorScheme.onBackground,
                                                        fontSize = 24.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Text(
                                                        text = if (selected) "已选中" else "点击使用",
                                                        color = if (selected) {
                                                            MaterialTheme.colorScheme.primary
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                        },
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }

                                        if (historyDurations.isEmpty()) {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 54.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "暂无历史",
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 15.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }

            }

            // 搴曢儴鍥哄畾鎸夐挳鍖哄煙锛堜笉鍙備笌婊氬姩锛屼笉鍙楄秺鐣屽洖寮瑰奖鍝嶏級
            val navBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val emphasisColor = MaterialTheme.colorScheme.primary
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp + navBottomPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 鐐圭姸鎸囩ず鍣?
                if (pagerState.pageCount > 1) {
                    Row(
                        modifier = Modifier.padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pagerState.pageCount) { index ->
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == index) {
                                            emphasisColor
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                        }
                                    )
                            )
                        }
                    }
                }

                val canStartTimer = selectedDurationMsRaw > 0L
                ActionSheetProgressButton(
                    text = "开始",
                    activeText = "关闭",
                    isActive = isTimerRunning,
                    progress = timerProgress,
                    onClick = {
                        if (isTimerRunning) {
                            onClearTimer()
                        } else {
                            onSetTimer(selectedDurationMsRaw, extendToSongEndEnabled)
                        }
                    },
                    enabled = isTimerRunning || canStartTimer,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun WheelNumberPickerColumn(
    valueRange: IntRange,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    val context = LocalContext.current
    val view = LocalView.current
    val clampedValue = selectedValue.coerceIn(valueRange.first, valueRange.last)
    val latestOnValueChange by rememberUpdatedState(onValueChange)
    val itemHeight = 42.dp
    val visibleRows = 3
    val renderSideRows = 5
    val edgePeek = itemHeight / 2
    val wheelViewportHeight = itemHeight * visibleRows + edgePeek * 2
    val rangeSize = valueRange.last - valueRange.first + 1
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val edgePeekPx = with(LocalDensity.current) { edgePeek.toPx() }
    val centerRowTopPx = edgePeekPx + ((visibleRows - 1) / 2f) * itemHeightPx
    var dragOffsetPx by remember { mutableStateOf(0f) }
    var internalValue by remember(valueRange.first, valueRange.last) { mutableIntStateOf(clampedValue) }
    var isDragging by remember { mutableStateOf(false) }
    var estimatedVelocityPxPerSec by remember { mutableStateOf(0f) }
    var lastDragTimestampNs by remember { mutableStateOf(0L) }
    var gestureNetTravelPx by remember { mutableStateOf(0f) }
    var gestureAbsTravelPx by remember { mutableStateOf(0f) }
    var lastHapticTimestampMs by remember { mutableStateOf(0L) }
    var settleJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val motionAnim = remember { Animatable(0f) }
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
    }

    fun wrap(value: Int): Int {
        val raw = value - valueRange.first
        val normalized = ((raw % rangeSize) + rangeSize) % rangeSize
        return valueRange.first + normalized
    }

    fun valueWithShift(center: Int, shift: Int): Int = wrap(center + shift)

    fun emitValue(newValue: Int) {
        if (newValue != internalValue) {
            internalValue = newValue
            latestOnValueChange(newValue)
            val now = System.currentTimeMillis()
            if (now - lastHapticTimestampMs >= 18L) {
                val vibrated = try {
                    if (vibrator?.hasVibrator() == true) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(8L)
                        }
                        true
                    } else {
                        false
                    }
                } catch (_: Throwable) {
                    false
                }
                if (!vibrated) {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
                lastHapticTimestampMs = now
            }
        }
    }

    fun applyOffsetDelta(rawDeltaPx: Float, useResistance: Boolean) {
        if (rawDeltaPx == 0f) return
        val appliedDelta = if (useResistance) {
            val normalized = (dragOffsetPx.absoluteValue / itemHeightPx).coerceAtLeast(0f)
            val resistance = 1f / (1f + normalized.pow(1.35f))
            rawDeltaPx * (0.8f + 0.2f * resistance)
        } else {
            rawDeltaPx
        }
        dragOffsetPx += appliedDelta

        while (dragOffsetPx >= itemHeightPx) {
            dragOffsetPx -= itemHeightPx
            emitValue(valueWithShift(internalValue, -1))
        }
        while (dragOffsetPx <= -itemHeightPx) {
            dragOffsetPx += itemHeightPx
            emitValue(valueWithShift(internalValue, 1))
        }
    }

    suspend fun animateDeltaTween(deltaPx: Float, durationMs: Int) {
        if (deltaPx.absoluteValue < 0.1f) return
        motionAnim.stop()
        motionAnim.snapTo(0f)
        var previous = 0f
        motionAnim.animateTo(
            targetValue = deltaPx,
            animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing)
        ) {
            val frameDelta = value - previous
            previous = value
            applyOffsetDelta(frameDelta, useResistance = false)
        }
    }

    suspend fun animateDeltaSpring(deltaPx: Float) {
        if (deltaPx.absoluteValue < 0.1f) return
        motionAnim.stop()
        motionAnim.snapTo(0f)
        var previous = 0f
        motionAnim.animateTo(
            targetValue = deltaPx,
            animationSpec = spring(
                dampingRatio = 0.86f,
                stiffness = Spring.StiffnessMedium
            )
        ) {
            val frameDelta = value - previous
            previous = value
            applyOffsetDelta(frameDelta, useResistance = false)
        }
    }

    suspend fun settleWithPhysics(
        releaseVelocityPxPerSec: Float,
        netTravelPx: Float,
        absTravelPx: Float
    ) {
        val velocityRowsPerSec = (releaseVelocityPxPerSec / itemHeightPx).coerceIn(-24f, 24f)
        val netTravelRows = (netTravelPx / itemHeightPx).coerceIn(-12f, 12f)
        val absTravelRows = (absTravelPx / itemHeightPx).coerceIn(0f, 20f)
        val offsetRatio = dragOffsetPx / itemHeightPx

        val direction = when {
            netTravelRows.absoluteValue > 0.08f -> if (netTravelRows >= 0f) 1f else -1f
            velocityRowsPerSec.absoluteValue > 0.2f -> if (velocityRowsPerSec >= 0f) 1f else -1f
            else -> if (dragOffsetPx >= 0f) 1f else -1f
        }

        val speed = velocityRowsPerSec.absoluteValue
        val isShortGesture = absTravelRows < 1.25f
        val isSlowGesture = speed < 2.2f && absTravelRows < 2f

        if (isSlowGesture) {
            val nearestSteps = (dragOffsetPx / itemHeightPx).roundToInt()
            if (nearestSteps != 0) {
                emitValue(valueWithShift(internalValue, -nearestSteps))
            }
            dragOffsetPx = 0f
            estimatedVelocityPxPerSec = 0f
            lastDragTimestampNs = 0L
            gestureNetTravelPx = 0f
            gestureAbsTravelPx = 0f
            isDragging = false
            return
        }

        val inertiaRows = when {
            isShortGesture -> {
                val cappedSpeed = speed.coerceAtMost(2.6f)
                val snapBias = offsetRatio * 0.9f
                val glideBias = direction * cappedSpeed * 0.08f
                val base = (snapBias + glideBias).coerceIn(-1.1f, 1.1f)
                if (base.absoluteValue < 0.18f) 0f else base
            }
            speed > 8f -> {
                val velocityContribution = direction * speed.pow(0.88f) * 0.95f
                val travelContribution = direction * absTravelRows.pow(0.45f) * 0.26f
                (velocityContribution + travelContribution).coerceIn(-7f, 7f)
            }
            speed > 3f -> {
                val velocityContribution = direction * speed.pow(0.82f) * 0.62f
                val travelContribution = direction * absTravelRows.pow(0.35f) * 0.2f
                (velocityContribution + travelContribution + offsetRatio * 0.4f).coerceIn(-3.2f, 3.2f)
            }
            else -> {
                (offsetRatio * 0.95f + direction * speed * 0.14f).coerceIn(-1.4f, 1.4f)
            }
        }

        val inertiaDeltaPx = inertiaRows * itemHeightPx
        val inertiaDuration = when {
            isShortGesture -> (90f + inertiaRows.absoluteValue * 42f).roundToInt().coerceIn(90, 170)
            speed > 8f -> (190f + inertiaRows.absoluteValue * 24f).roundToInt().coerceIn(190, 420)
            speed > 3f -> (140f + inertiaRows.absoluteValue * 26f).roundToInt().coerceIn(140, 280)
            else -> (110f + inertiaRows.absoluteValue * 40f).roundToInt().coerceIn(110, 190)
        }

        animateDeltaTween(inertiaDeltaPx, durationMs = inertiaDuration)
        animateDeltaSpring(deltaPx = -dragOffsetPx)

        if (dragOffsetPx.absoluteValue < 0.5f) {
            dragOffsetPx = 0f
        }
        estimatedVelocityPxPerSec = 0f
        lastDragTimestampNs = 0L
        gestureNetTravelPx = 0f
        gestureAbsTravelPx = 0f
        isDragging = false
    }

    LaunchedEffect(clampedValue) {
        if (!isDragging && clampedValue != internalValue) {
            internalValue = clampedValue
        }
    }

    Box(
        modifier = modifier
            .width(74.dp)
            .height(wheelViewportHeight)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Black.copy(alpha = 0f),
                            0.06f to Color.Black.copy(alpha = 0.35f),
                            0.14f to Color.Black.copy(alpha = 1f),
                            0.86f to Color.Black.copy(alpha = 1f),
                            0.94f to Color.Black.copy(alpha = 0.35f),
                            1.00f to Color.Black.copy(alpha = 0f)
                        )
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
            .clipToBounds()
            .pointerInput(valueRange.first, valueRange.last) {
                detectVerticalDragGestures(
                    onDragStart = {
                        settleJob?.cancel()
                        scope.launch {
                            motionAnim.stop()
                            motionAnim.snapTo(0f)
                        }
                        isDragging = true
                        estimatedVelocityPxPerSec = 0f
                        lastDragTimestampNs = 0L
                        gestureNetTravelPx = 0f
                        gestureAbsTravelPx = 0f
                    },
                    onDragEnd = {
                        val releaseVelocity = estimatedVelocityPxPerSec
                        val netTravel = gestureNetTravelPx
                        val absTravel = gestureAbsTravelPx
                        settleJob?.cancel()
                        settleJob = scope.launch {
                            settleWithPhysics(releaseVelocity, netTravel, absTravel)
                        }
                    },
                    onDragCancel = {
                        settleJob?.cancel()
                        settleJob = scope.launch {
                            settleWithPhysics(0f, gestureNetTravelPx, gestureAbsTravelPx)
                        }
                    }
                ) { _, dragAmount ->
                    val nowNs = System.nanoTime()
                    if (lastDragTimestampNs != 0L) {
                        val dt = (nowNs - lastDragTimestampNs) / 1_000_000_000f
                        if (dt in 0.008f..0.08f) {
                            val instantVelocity = dragAmount / dt
                            val clampedInstantVelocity = instantVelocity.coerceIn(-itemHeightPx * 24f, itemHeightPx * 24f)
                            estimatedVelocityPxPerSec =
                                estimatedVelocityPxPerSec * 0.82f + clampedInstantVelocity * 0.18f
                        }
                    }
                    lastDragTimestampNs = nowNs
                    gestureNetTravelPx += dragAmount
                    gestureAbsTravelPx += dragAmount.absoluteValue
                    applyOffsetDelta(rawDeltaPx = dragAmount, useResistance = true)
                }
            }
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(10.dp),
            shadowElevation = 0.dp,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 2.dp)
        ) {
            Box {}
        }

        (-renderSideRows..renderSideRows).forEach { shift ->
            val y = (centerRowTopPx + shift * itemHeightPx + dragOffsetPx).roundToInt()
            val relative = (shift + (dragOffsetPx / itemHeightPx)).absoluteValue
            val isSelectedRow = relative < 0.5f
            val alpha = when {
                isSelectedRow -> 1f
                relative < 1.5f -> 0.9f
                relative < 2.5f -> 0.5f
                relative < 3.5f -> 0.22f
                else -> 0.08f
            }
            val scale = when {
                relative < 0.5f -> 1f
                relative < 1.5f -> 0.97f
                relative < 2.5f -> 0.93f
                relative < 3.5f -> 0.9f
                else -> 0.88f
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .offset { IntOffset(0, y) },
                contentAlignment = Alignment.Center
            ) {
                val textColor = when {
                    isActive && isSelectedRow -> MaterialTheme.colorScheme.primary
                    isActive -> MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.5f)
                    else -> MaterialTheme.colorScheme.onBackground.copy(alpha = alpha)
                }
                Text(
                    text = String.format("%02d", valueWithShift(internalValue, shift)),
                    color = textColor,
                    fontSize = if (isSelectedRow) 38.sp else 34.sp,
                    lineHeight = if (isSelectedRow) 38.sp else 34.sp,
                    fontWeight = if (isSelectedRow) FontWeight.SemiBold else FontWeight.Medium,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    modifier = Modifier
                        .offset(y = if (isSelectedRow) (-4).dp else (-2).dp)
                        .scale(scale)
                )
            }
        }
    }
}

private fun formatSleepTimerRemaining(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private fun formatDurationClock(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

