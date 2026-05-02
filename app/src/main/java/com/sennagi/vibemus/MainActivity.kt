package com.sennagi.vibemus

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sennagi.vibemus.music.FolderChoice
import com.sennagi.vibemus.music.MusicFolderPreferences
import com.sennagi.vibemus.music.MusicLibrary
import com.sennagi.vibemus.music.MusicUserPreferences
import com.sennagi.vibemus.music.SongItem
import com.sennagi.vibemus.player.LocalPlaybackController
import com.sennagi.vibemus.ui.components.MgBlurBottomBar
import com.sennagi.vibemus.ui.components.MgBottomBarItem
import com.sennagi.vibemus.ui.components.MiniPlayer
import com.sennagi.vibemus.ui.screens.PlayerOverlayScreen
import com.sennagi.vibemus.ui.theme.CardWhite
import com.sennagi.vibemus.ui.theme.FavoriteRed
import com.sennagi.vibemus.ui.theme.SoftInk
import com.sennagi.vibemus.ui.theme.VibeMusTheme
import com.sennagi.senui.components.ActionSheet
import com.sennagi.senui.components.ActionSheetButton
import com.sennagi.senui.components.CheckPill
import com.sennagi.senui.components.MediaListItemCard
import com.sennagi.senui.components.SheetBounceContainer
import com.sennagi.senui.components.SecondaryTopBar
import com.sennagi.senui.components.bounceOverscroll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import kotlin.math.max
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = AndroidColor.TRANSPARENT,
                darkScrim = AndroidColor.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = AndroidColor.TRANSPARENT,
                darkScrim = AndroidColor.TRANSPARENT
            )
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        } else {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
        setContent {
            VibeMusTheme {
                VibeMusApp()
            }
        }
    }
}

private val bottomBarItems = listOf(
    MgBottomBarItem(0, Icons.Rounded.Star, "姝屾洸"),
    MgBottomBarItem(1, Icons.Rounded.Person, "鎴戠殑")
)

private enum class LibraryScreen(
    val title: String
) {
    AllSongs("\u6240\u6709\u6b4c\u66f2"),
    RecentHistory("\u5386\u53f2\u64ad\u653e"),
    Favorites("\u6211\u7684\u6536\u85cf")
}

@Composable
fun VibeMusApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val folderPreferences = remember(context) { MusicFolderPreferences(context) }
    val userPreferences = remember(context) { MusicUserPreferences(context) }
    var recentPlayIds by remember { mutableStateOf(userPreferences.getRecentPlayIds()) }
    var favoriteIds by remember { mutableStateOf(userPreferences.getFavoriteIds()) }
    var savedFolderCount by remember { mutableIntStateOf(folderPreferences.getSelectedFolders().size) }

    val playbackController = rememberPlaybackController(context) { song ->
        userPreferences.pushRecentPlay(song.id)
        userPreferences.saveLastPlayedId(song.id)
        recentPlayIds = userPreferences.getRecentPlayIds()
    }

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var visibleSongs by remember { mutableStateOf<List<SongItem>>(emptyList()) }
    var pendingSongs by remember { mutableStateOf<List<SongItem>>(emptyList()) }
    var folderChoices by remember { mutableStateOf<List<FolderChoice>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var helperText by remember { mutableStateOf<String?>(null) }
    var activeLibraryScreen by rememberSaveable { mutableStateOf<LibraryScreen?>(null) }
    var showNowPlaying by rememberSaveable { mutableStateOf(false) }
    var sleepTimerSelectedDurationMs by rememberSaveable { mutableLongStateOf(120L * 60_000L) }
    var sleepTimerEndAtMs by rememberSaveable { mutableLongStateOf(0L) }
    var sleepTimerExtendToSongEnd by rememberSaveable { mutableStateOf(false) }
    var sleepTimerAwaitingSongEnd by remember { mutableStateOf(false) }
    var sleepTimerExtensionSongId by rememberSaveable { mutableLongStateOf(-1L) }
    var sleepTimerRemainingMs by remember { mutableLongStateOf(0L) }
    var sleepTimerNotice by remember { mutableStateOf<String?>(null) }
    val sleepTimerHistoryMs = rememberSaveable(
        saver = listSaver(
            save = { stateList -> stateList.toList() },
            restore = { restored ->
                mutableStateListOf<Long>().apply { addAll(restored) }
            }
        )
    ) {
        mutableStateListOf<Long>()
    }

    fun playFromLibrary(song: SongItem) {
        val index = visibleSongs.indexOfFirst { it.id == song.id }
        if (index >= 0) {
            playbackController.setQueue(visibleSongs, index, autoplay = true)
        }
    }

    fun toggleFavorite(songId: Long) {
        userPreferences.toggleFavorite(songId)
        favoriteIds = userPreferences.getFavoriteIds()
    }

    fun clearSleepTimer(notice: String? = null) {
        sleepTimerEndAtMs = 0L
        sleepTimerRemainingMs = 0L
        sleepTimerAwaitingSongEnd = false
        sleepTimerExtensionSongId = -1L
        if (notice != null) {
            sleepTimerNotice = notice
        }
    }

    fun applyVisibleSongs(scannedSongs: List<SongItem>, selectedFolders: Set<String>) {
        visibleSongs = MusicLibrary.filterSongsByFolders(scannedSongs, selectedFolders)
        helperText = if (selectedFolders.isNotEmpty() && visibleSongs.isEmpty()) {
            "\u6682\u672a\u5728\u4f60\u4fdd\u7559\u7684\u6587\u4ef6\u5939\u91cc\u627e\u5230\u6b4c\u66f2"
        } else {
            null
        }
    }

    fun startScanFlow(forceFolderSelection: Boolean = false) {
        scope.launch {
            isScanning = true
            val scannedSongs = withContext(Dispatchers.IO) {
                MusicLibrary.scanDevice(context)
            }
            isScanning = false

            if (scannedSongs.isEmpty()) {
                visibleSongs = emptyList()
                helperText = "\u6ca1\u6709\u626b\u63cf\u5230\u53ef\u7528\u97f3\u4e50"
                folderPreferences.clearSelectedFolders()
                savedFolderCount = 0
                return@launch
            }

            val savedFolders = folderPreferences.getSelectedFolders()
            if (savedFolders.isNotEmpty() && !forceFolderSelection) {
                applyVisibleSongs(scannedSongs, savedFolders)
                return@launch
            }

            pendingSongs = scannedSongs
            folderChoices = MusicLibrary.buildFolderChoices(scannedSongs, savedFolders)
            helperText = null
        }
    }

    fun refreshSavedLibrary() {
        if (!hasAudioPermission(context)) {
            visibleSongs = emptyList()
            return
        }

        scope.launch {
            isScanning = true
            val scannedSongs = withContext(Dispatchers.IO) {
                MusicLibrary.scanDevice(context)
            }
            val savedFolders = folderPreferences.getSelectedFolders()
            savedFolderCount = savedFolders.size
            if (savedFolders.isEmpty()) {
                visibleSongs = emptyList()
            } else {
                applyVisibleSongs(scannedSongs, savedFolders)
            }
            isScanning = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startScanFlow()
        } else {
            helperText = "\u9700\u8981\u97f3\u9891\u8bfb\u53d6\u6743\u9650\u624d\u80fd\u626b\u63cf\u672c\u5730\u97f3\u4e50"
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (!hasNotificationPermission(context)) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        refreshSavedLibrary()
    }

    LaunchedEffect(visibleSongs) {
        val lastPlayedId = userPreferences.getLastPlayedId()
        if (playbackController.currentSong == null && lastPlayedId != null) {
            val index = visibleSongs.indexOfFirst { it.id == lastPlayedId }
            if (index >= 0) {
                playbackController.setQueue(visibleSongs, index, autoplay = false)
            }
        }
    }

    LaunchedEffect(sleepTimerEndAtMs) {
        if (sleepTimerEndAtMs <= 0L) {
            if (!sleepTimerAwaitingSongEnd) {
                sleepTimerRemainingMs = 0L
            }
            return@LaunchedEffect
        }

        while (sleepTimerEndAtMs > 0L) {
            val remainingMs = (sleepTimerEndAtMs - System.currentTimeMillis()).coerceAtLeast(0L)
            sleepTimerRemainingMs = remainingMs
            if (remainingMs == 0L) {
                val currentSong = playbackController.currentSong
                val activeDurationMs = playbackController.durationMs
                    .takeIf { it > 0L }
                    ?: currentSong?.durationMs
                    ?: 0L
                val songRemainingMs = (activeDurationMs - playbackController.currentPositionMs).coerceAtLeast(0L)

                if (sleepTimerExtendToSongEnd &&
                    playbackController.isPlaying &&
                    currentSong != null &&
                    songRemainingMs > 1_500L
                ) {
                    sleepTimerEndAtMs = 0L
                    sleepTimerAwaitingSongEnd = true
                    sleepTimerExtensionSongId = currentSong.id
                    sleepTimerRemainingMs = songRemainingMs
                } else {
                    clearSleepTimer()
                    if (playbackController.isPlaying) {
                        playbackController.togglePlayPause()
                    }
                }
                break
            }
            delay(minOf(1_000L, remainingMs))
        }
    }

    LaunchedEffect(
        sleepTimerAwaitingSongEnd,
        playbackController.currentSong?.id,
        playbackController.currentPositionMs,
        playbackController.durationMs,
        playbackController.isPlaying
    ) {
        if (!sleepTimerAwaitingSongEnd) {
            return@LaunchedEffect
        }

        val currentSong = playbackController.currentSong
        if (currentSong == null) {
            clearSleepTimer()
            return@LaunchedEffect
        }

        if (currentSong.id != sleepTimerExtensionSongId) {
            if (playbackController.isPlaying) {
                playbackController.togglePlayPause()
            }
            clearSleepTimer(notice = "已因切歌取消定时关闭")
            return@LaunchedEffect
        }

        val activeDurationMs = playbackController.durationMs
            .takeIf { it > 0L }
            ?: currentSong.durationMs
        val songRemainingMs = (activeDurationMs - playbackController.currentPositionMs).coerceAtLeast(0L)
        sleepTimerRemainingMs = songRemainingMs

        if (!playbackController.isPlaying) {
            if (songRemainingMs > 1_500L) {
                clearSleepTimer(notice = "已因手动暂停取消定时关闭")
            } else {
                clearSleepTimer()
            }
        }
    }

    val favoriteSongs = remember(visibleSongs, favoriteIds) {
        visibleSongs.filter { it.id in favoriteIds }
    }
    val recentPlaySongs = remember(visibleSongs, recentPlayIds) {
        songsFromIds(visibleSongs, recentPlayIds)
    }
    val appBottomBarItems = remember {
        listOf(
            MgBottomBarItem(0, Icons.Rounded.Star, "\u4e2a\u6027"),
            MgBottomBarItem(1, Icons.Rounded.MusicNote, "\u66f2\u5e93"),
            MgBottomBarItem(2, Icons.Rounded.Person, "\u6211\u7684")
        )
    }
    val configuration = LocalConfiguration.current
    // 参考 MGAide：屏幕宽度 >= 600dp 视为大屏，使用左侧栏导航
    val isWideScreen = configuration.screenWidthDp >= 600
    var userSideRailCollapsed by rememberSaveable { mutableStateOf(false) }
    // 大屏时默认使用左侧栏，不强制使用顶部导航栏
    val useTopNavigationMode = userSideRailCollapsed
    val sideRailWidth by animateDpAsState(
        targetValue = if (!isWideScreen || useTopNavigationMode) 0.dp else 280.dp,
        animationSpec = tween(
            durationMillis = 420,
            easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        ),
        label = "vibemusSideRailWidth"
    )
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    // 左侧栏模式下，第一个选项卡片顶部位置 = statusBar + 4dp + Menu按钮(48dp) + 4dp
    val sideRailFirstItemTop = statusBarHeight + 56.dp
    // 顶部导航栏模式时，顶栏底部位置（用于计算内容初始偏移）
    val topNavBottomOffset = statusBarHeight + 64.dp
    // 曲库页顶部导航栏更高（含标签栏）
    val libraryTopNavBottomOffset = statusBarHeight + 112.dp

    val wideTopNavHeight by animateDpAsState(
        targetValue = if (isWideScreen && useTopNavigationMode) {
            if (selectedTabIndex == 1) libraryTopNavBottomOffset else topNavBottomOffset
        } else 0.dp,
        animationSpec = tween(
            durationMillis = 380,
            easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        ),
        label = "vibemusWideTopNavHeight"
    )
    val libraryTabs = remember { listOf("\u5168\u90e8", "\u6b4c\u5355", "\u4e13\u8f91", "\u6587\u4ef6\u5939") }
    var librarySelectedTab by rememberSaveable { mutableIntStateOf(0) }

    // Animation state for PlayerOverlay (MGAide style)
    val density = LocalDensity.current
    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels
    val screenHeightPx = with(density) { screenHeight.toFloat() }
    val offsetY = remember { Animatable(screenHeightPx) }
    var lastScreenHeightPx by remember { mutableFloatStateOf(screenHeightPx) }

    // Animate PlayerOverlay when showNowPlaying changes
    LaunchedEffect(showNowPlaying) {
        if (showNowPlaying) {
            offsetY.animateTo(0f, animationSpec = spring(stiffness = 600f))
        } else {
            offsetY.animateTo(screenHeightPx, animationSpec = spring(stiffness = 600f))
        }
    }

    // Handle screen height changes
    LaunchedEffect(screenHeightPx) {
        val oldHeight = lastScreenHeightPx
        if (oldHeight > 0f && oldHeight != screenHeightPx) {
            val progress = (offsetY.value / oldHeight).coerceIn(0f, 1f)
            offsetY.snapTo(progress * screenHeightPx)
        }
        lastScreenHeightPx = screenHeightPx
    }

    // Calculate overlay offset
    val overlayOffsetY = run {
        val isPreviouslyHidden = !showNowPlaying && offsetY.value >= (lastScreenHeightPx - 1f)
        if (isPreviouslyHidden) screenHeightPx else offsetY.value.coerceIn(0f, screenHeightPx)
    }

    // MGAide-style layout: content area + adaptive nav (bottom bar on phone, side rail on large screen)
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isWideScreen && !useTopNavigationMode,
            enter = slideInHorizontally(
                initialOffsetX = { -it / 2 },
                animationSpec = tween(durationMillis = 420, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
            ) + fadeIn(
                animationSpec = tween(durationMillis = 340, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(durationMillis = 320, easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f))
            ) + fadeOut(
                animationSpec = tween(durationMillis = 260, easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f))
            ),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            VibeMusSideNavigationRail(
                items = appBottomBarItems,
                selectedId = selectedTabIndex,
                onItemClick = { selectedTabIndex = it },
                onCollapseClick = {
                    userSideRailCollapsed = true
                },
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = sideRailWidth)
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (!isWideScreen) {
                        MgBlurBottomBar(
                            items = appBottomBarItems,
                            selectedId = selectedTabIndex,
                            onItemClick = { selectedTabIndex = it },
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 大屏模式下，内容顶距固定为左侧栏第一个选项卡片位置
                        // 顶部导航栏从上方滑下覆盖内容顶部，内容本身不移动
                        // 手机版顶距与渐进式遮罩高度一致减去60dp（88.dp）
                        val contentTopOffset = if (isWideScreen) sideRailFirstItemTop else 88.dp

                        when (selectedTabIndex) {
                            0 -> PersonalityScreen(
                                songs = visibleSongs,
                                isScanning = isScanning,
                                helperText = helperText,
                                contentTopPadding = contentTopOffset,
                                isWideScreen = isWideScreen,
                                useTopNavigationMode = useTopNavigationMode,
                                onScanClick = {
                                    if (hasAudioPermission(context)) {
                                        startScanFlow()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                                    }
                                },
                                onPlaySong = { playFromLibrary(it) }
                            )

                            1 -> MusicLibraryPage(
                                songs = visibleSongs,
                                favoriteIds = favoriteIds,
                                isScanning = isScanning,
                                helperText = helperText,
                                contentTopPadding = contentTopOffset,
                                isWideScreen = isWideScreen,
                                useTopNavigationMode = useTopNavigationMode,
                                onScanClick = {
                                    if (hasAudioPermission(context)) {
                                        startScanFlow()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                                    }
                                },
                                onPlaySong = { playFromLibrary(it) },
                                onToggleFavorite = { toggleFavorite(it) }
                            )

                            2 -> MineScreen(
                                favoriteSongs = favoriteSongs,
                                recentPlaySongs = recentPlaySongs,
                                savedFolderCount = savedFolderCount,
                                contentTopPadding = contentTopOffset,
                                isWideScreen = isWideScreen,
                                useTopNavigationMode = useTopNavigationMode,
                                onOpenFavorites = { activeLibraryScreen = LibraryScreen.Favorites },
                                onRescan = {
                                    if (hasAudioPermission(context)) {
                                        startScanFlow()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                                    }
                                },
                                onManageFolders = {
                                    if (hasAudioPermission(context)) {
                                        startScanFlow(forceFolderSelection = true)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                                    }
                                },
                                onPlaySong = { playFromLibrary(it) }
                            )
                        }
                    }

                    activeLibraryScreen?.let { screen ->
                        SongCollectionScreen(
                            title = screen.title,
                            songs = when (screen) {
                                LibraryScreen.AllSongs -> visibleSongs
                                LibraryScreen.RecentHistory -> recentPlaySongs
                                LibraryScreen.Favorites -> favoriteSongs
                            },
                            favoriteIds = favoriteIds,
                            onBack = { activeLibraryScreen = null },
                            onPlaySong = { playFromLibrary(it) },
                            onToggleFavorite = { toggleFavorite(it) }
                        )
                    }

                    // 全局共用顶栏背景（渐进式遮罩）
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(148.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                                        0.35f to MaterialTheme.colorScheme.background.copy(alpha = 0.75f),
                                        0.65f to MaterialTheme.colorScheme.background.copy(alpha = 0.35f),
                                        1.0f to MaterialTheme.colorScheme.background.copy(alpha = 0.0f)
                                    )
                                )
                            )
                            .zIndex(1f)
                    )

                    // 顶部导航栏按钮（仅在顶部导航栏模式下显示）
                    AnimatedVisibility(
                        visible = isWideScreen && useTopNavigationMode,
                        enter = slideInVertically(
                            initialOffsetY = { -it / 2 },
                            animationSpec = tween(durationMillis = 380, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = 300, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { -it },
                            animationSpec = tween(durationMillis = 280, easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f))
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 220, easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f))
                        ),
                        modifier = Modifier.align(Alignment.TopCenter).zIndex(2f)
                    ) {
                        WideScreenTopNavigationBar(
                            items = appBottomBarItems,
                            selectedId = selectedTabIndex,
                            onItemClick = { selectedTabIndex = it },
                            onExpandSideRail = {
                                userSideRailCollapsed = false
                            }
                        )
                    }

                    // 曲库页标签按钮（在遮罩之上，zIndex 3）
                    // 手机竖屏时也显示
                    if (selectedTabIndex == 1) {
                        LibraryTopBarContent(
                            useTopNavigationMode = useTopNavigationMode,
                            isWideScreen = isWideScreen,
                            modifier = Modifier.align(Alignment.TopCenter).zIndex(3f)
                        )
                    }
                }
            }

        }

        val shouldShowMiniPlayer =
            playbackController.currentSong != null &&
                !showNowPlaying &&
                overlayOffsetY >= (screenHeightPx - 1f)

        AnimatedVisibility(
            visible = shouldShowMiniPlayer,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 260, delayMillis = 30)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it / 2 },
                animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 110)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            MiniPlayer(
                song = playbackController.currentSong,
                isPlaying = playbackController.isPlaying,
                onPreviousClick = { playbackController.playPrevious() },
                onPlayPauseClick = { playbackController.togglePlayPause() },
                onNextClick = { playbackController.playNext() },
                onExpand = { showNowPlaying = true },
                modifier = Modifier
                    .then(
                        if (isWideScreen) {
                            Modifier.width(480.dp)
                        } else {
                            Modifier.fillMaxWidth()
                        }
                    )
                    .padding(bottom = if (isWideScreen) 20.dp else 76.dp)
            )
        }

        if (showNowPlaying || overlayOffsetY < screenHeightPx) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, overlayOffsetY.roundToInt()) }
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                val threshold = screenHeightPx * 0.15f
                                if (offsetY.value > threshold) {
                                    showNowPlaying = false
                                } else {
                                    scope.launch {
                                        offsetY.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessLow))
                                    }
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                val newOffset = (offsetY.value + dragAmount).coerceIn(0f, screenHeightPx)
                                scope.launch {
                                    offsetY.snapTo(newOffset)
                                }
                            }
                        )
                    }
            ) {
                PlayerOverlayScreen(
                    song = playbackController.currentSong,
                    isPlaying = playbackController.isPlaying,
                    isLoading = playbackController.isLoading,
                    isFavorite = playbackController.currentSong?.id in favoriteIds,
                    currentPositionMs = playbackController.currentPositionMs,
                    durationMs = playbackController.durationMs.takeIf { it > 0L }
                        ?: playbackController.currentSong?.durationMs ?: 0L,
                    playlist = playbackController.playlist,
                    currentSongIndex = playbackController.currentIndex,
                    playMode = playbackController.playMode,
                    onDismiss = { showNowPlaying = false },
                    onToggleFavorite = { playbackController.currentSong?.let { toggleFavorite(it.id) } },
                    onPlayPrevious = { playbackController.playPrevious() },
                    onTogglePlayback = { playbackController.togglePlayPause() },
                    onPlayNext = { playbackController.playNext() },
                    onSeek = { playbackController.seekTo(it) },
                    onPlaySongFromPlaylist = { song ->
                        val index = playbackController.playlist.indexOfFirst { it.id == song.id }
                        if (index >= 0) {
                            playbackController.playAt(index)
                        }
                    },
                    onTogglePlayMode = { playbackController.togglePlayMode() },
                    sleepTimerRemainingMs = sleepTimerRemainingMs,
                    sleepTimerDurationMs = sleepTimerSelectedDurationMs,
                    sleepTimerHistory = sleepTimerHistoryMs,
                    sleepTimerExtendToSongEnd = sleepTimerExtendToSongEnd,
                    sleepTimerAwaitingSongEnd = sleepTimerAwaitingSongEnd,
                    onSetSleepTimer = { durationMs, extendToSongEnd ->
                        val updatedHistory = buildList {
                            add(durationMs)
                            addAll(sleepTimerHistoryMs.filter { it != durationMs })
                        }.take(12)
                        sleepTimerHistoryMs.clear()
                        sleepTimerHistoryMs.addAll(updatedHistory)
                        sleepTimerSelectedDurationMs = durationMs
                        sleepTimerExtendToSongEnd = extendToSongEnd
                        sleepTimerAwaitingSongEnd = false
                        sleepTimerExtensionSongId = -1L
                        sleepTimerEndAtMs = System.currentTimeMillis() + durationMs
                        sleepTimerRemainingMs = durationMs
                    },
                    onClearSleepTimer = { clearSleepTimer() }
                )
            }
        }

        if (!sleepTimerNotice.isNullOrBlank()) {
            AlertDialog(
                onDismissRequest = { sleepTimerNotice = null },
                confirmButton = {
                    TextButton(onClick = { sleepTimerNotice = null }) {
                        Text("知道了")
                    }
                },
                title = {
                    Text("定时关闭已取消")
                },
                text = {
                    Text(sleepTimerNotice.orEmpty())
                }
            )
        }
    }

    if (folderChoices.isNotEmpty()) {
        FolderSelectionDialog(
            folders = folderChoices,
            onDismiss = {
                folderChoices = emptyList()
                pendingSongs = emptyList()
            },
            onFolderToggle = { path ->
                folderChoices = folderChoices.map { folder ->
                    if (folder.path == path) folder.copy(selected = !folder.selected) else folder
                }
            },
            onConfirm = {
                val selectedFolders = folderChoices
                    .filter { it.selected }
                    .map { it.path }
                    .toSet()

                folderPreferences.saveSelectedFolders(selectedFolders)
                savedFolderCount = selectedFolders.size
                applyVisibleSongs(pendingSongs, selectedFolders)
                folderChoices = emptyList()
                pendingSongs = emptyList()
            }
        )
    }
}

@Composable
private fun PersonalityScreen(
    songs: List<SongItem>,
    isScanning: Boolean,
    helperText: String?,
    contentTopPadding: androidx.compose.ui.unit.Dp = 0.dp,
    isWideScreen: Boolean = false,
    useTopNavigationMode: Boolean = false,
    onScanClick: () -> Unit,
    onPlaySong: (SongItem) -> Unit
) {
    if (songs.isEmpty()) {
        EmptyLibraryState(
            isScanning = isScanning,
            helperText = helperText,
            onScanClick = onScanClick
        )
        return
    }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    // 手机固定2列，大屏根据宽度计算列数（每列最小160dp）
    val columnCount = if (isWideScreen) {
        max(2, (screenWidthDp / 160.dp).toInt())
    } else {
        2
    }
    val horizontalPadding = 16.dp
    val spacing = 12.dp
    val itemWidth = (screenWidthDp - horizontalPadding * 2 - spacing * (columnCount - 1)) / columnCount

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .bounceOverscroll(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(contentTopPadding)
                )
            }

            item {
                PrimarySectionHeader(title = "\u5168\u90e8\u6b4c\u66f2")
            }

            // 网格化排布歌曲，只显示专辑图
            val rows = songs.chunked(columnCount)
            items(rows.size) { rowIndex ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    val rowSongs = rows[rowIndex]
                    rowSongs.forEach { song ->
                        AlbumCoverItem(
                            song = song,
                            size = itemWidth,
                            onClick = { onPlaySong(song) }
                        )
                    }
                    // 补齐空白位置
                    repeat(columnCount - rowSongs.size) {
                        Box(modifier = Modifier.width(itemWidth))
                    }
                }
            }

        }
    }
}

// 专辑封面网格项
@Composable
private fun AlbumCoverItem(
    song: SongItem,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val coverRequest = remember(song.albumArtUri) {
        coil.request.ImageRequest.Builder(context)
            .data(song.albumArtUri)
            .crossfade(false)
            .size(300)
            .build()
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (song.albumArtUri != null) {
            coil.compose.AsyncImage(
                model = coverRequest,
                contentDescription = song.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(size * 0.4f)
                )
            }
        }
    }
}

@Composable
private fun MusicLibraryPage(
    songs: List<SongItem>,
    favoriteIds: Set<Long>,
    isScanning: Boolean,
    helperText: String?,
    contentTopPadding: androidx.compose.ui.unit.Dp = 0.dp,
    isWideScreen: Boolean = false,
    useTopNavigationMode: Boolean = false,
    onScanClick: () -> Unit,
    onPlaySong: (SongItem) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {
    if (songs.isEmpty()) {
        EmptyLibraryState(
            isScanning = isScanning,
            helperText = helperText,
            onScanClick = onScanClick
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .bounceOverscroll(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(contentTopPadding)
                )
            }

            items(songs, key = { it.id }) { song ->
                LibrarySongRow(
                    song = song,
                    isFavorite = song.id in favoriteIds,
                    onClick = { onPlaySong(song) },
                    onToggleFavorite = { onToggleFavorite(song.id) }
                )
            }
        }
    }
}

@Composable
private fun LibraryTopBarContent(
    useTopNavigationMode: Boolean,
    isWideScreen: Boolean = false,
    modifier: Modifier = Modifier
) {
    var topTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showTabSelector by remember { mutableStateOf(false) }
    val tabs = remember {
        listOf("\u5168\u90e8", "\u6b4c\u5355", "\u4e13\u8f91", "\u6587\u4ef6\u5939")
    }

    // 手机竖屏时减少顶部padding，让按钮往上提
    val topPadding = if (isWideScreen) 8.dp else 4.dp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 16.dp, top = topPadding, end = 16.dp, bottom = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (useTopNavigationMode) {
            // 顶部导航栏模式：合并为下拉选择按钮（原比例，与导航栏水平居中）
            val selectedTabBg by animateColorAsState(
                targetValue = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                animationSpec = tween(240),
                label = "selectedTabBg"
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(selectedTabBg)
                    .clickable { showTabSelector = true }
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = tabs[topTabIndex],
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer(rotationZ = -90f)
                )
            }
        } else {
            // 左侧栏模式或手机竖屏：显示标签按钮行（原比例，与导航栏水平居中）
            LibraryTopCapsuleBar(
                tabs = tabs,
                selectedIndex = topTabIndex,
                onSelect = { topTabIndex = it }
            )
        }
    }

    // SenUI ActionSheet 弹窗选择 - 使用 ActionSheetCardList 组件
    if (showTabSelector) {
        ActionSheet(
            title = "\u5207\u6362\u5206\u7c7b",
            onDismiss = { showTabSelector = false }
        ) {
            Column {
                Box(modifier = Modifier.height(16.dp))
                ActionSheetCardList(
                    items = tabs,
                    selectedIndex = topTabIndex,
                    onItemSelected = { index ->
                        topTabIndex = index
                        showTabSelector = false
                    }
                )
                Box(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun LibraryTopCapsuleBar(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(tabs.size, key = { tabs[it] }) { index ->
                val isSelected = selectedIndex == index
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    label = "libraryTopTabBackground"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "libraryTopTabContent"
                )

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(backgroundColor)
                        .clickable { onSelect(index) }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabs[index],
                        color = contentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun VibeMusSideNavigationRail(
    items: List<MgBottomBarItem>,
    selectedId: Int,
    onItemClick: (Int) -> Unit,
    onCollapseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(modifier = Modifier.height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding()))
            Box(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapseClick) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = "Collapse",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Box(modifier = Modifier.height(4.dp))

            items.forEach { item ->
                val isSelected = selectedId == item.id
                val interactionSource = remember(item.id) { MutableInteractionSource() }
                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.background
                    },
                    label = "sideRailItemContainer"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    label = "sideRailItemContent"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(containerColor)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onItemClick(item.id) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Box(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.label,
                        color = contentColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun WideScreenTopNavigationBar(
    items: List<MgBottomBarItem>,
    selectedId: Int,
    onItemClick: (Int) -> Unit,
    onExpandSideRail: () -> Unit
) {
    val tabWidth = 108.dp
    val menuSlotWidth = 40.dp
    val tabSpacing = 4.dp
    val indicatorInset = 4.dp
    val iconSize = 18.dp
    val iconLabelGap = 8.dp
    val textExitShift = 8.dp
    val textStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val indicatorEasing = CubicBezierEasing(0.2f, 0.9f, 0.2f, 1f)
    val iconEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    val textEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val selectedIndex = remember(items, selectedId) {
        items.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
    }
    val tabsTrackWidth = (tabWidth * items.size) +
        (tabSpacing * (items.size - 1).coerceAtLeast(0))
    val tabsContainerWidth = tabsTrackWidth + (indicatorInset * 2) + menuSlotWidth + 16.dp
    val indicatorOffset by animateDpAsState(
        targetValue = (tabWidth + tabSpacing) * selectedIndex,
        animationSpec = spring(
            dampingRatio = 0.9f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "wideTopNavIndicatorOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.width(tabsContainerWidth),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier.height(44.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 菜单按钮 - 融合在胶囊最左侧
                    Box(
                        modifier = Modifier
                            .width(menuSlotWidth + 16.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp))
                            .clickable { onExpandSideRail() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = indicatorInset, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(x = indicatorOffset)
                                .width(tabWidth)
                                .height(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )

                        Row(
                            modifier = Modifier.width(tabsTrackWidth),
                            horizontalArrangement = Arrangement.spacedBy(tabSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items.forEach { item ->
                                val isSelected = item.id == selectedId
                                val interactionSource = remember(item.id) { MutableInteractionSource() }
                                val contentColor by animateColorAsState(
                                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    animationSpec = tween(durationMillis = 320, easing = textEasing),
                                    label = "wideTopNavContent"
                                )
                                val selectionProgress: Float by animateFloatAsState(
                                    targetValue = if (isSelected) 1f else 0f,
                                    animationSpec = tween(durationMillis = 360, easing = iconEasing),
                                    label = "wideTopNavSelectionProgress"
                                )
                                val labelWidthPx = remember(item.label, textStyle, density) {
                                    textMeasurer.measure(
                                        text = AnnotatedString(item.label),
                                        style = textStyle,
                                        maxLines = 1
                                    ).size.width.toFloat()
                                }
                                val iconSizePx = with(density) { iconSize.toPx() }
                                val gapPx = with(density) { iconLabelGap.toPx() }
                                val exitShiftPx = with(density) { textExitShift.toPx() }
                                val selectedIconCenterShiftPx = -((labelWidthPx + gapPx) / 2f)
                                val selectedTextCenterShiftPx = (iconSizePx + gapPx) / 2f
                                val iconOffset = with(density) {
                                    (selectedIconCenterShiftPx * selectionProgress).toDp()
                                }
                                val textCenterOffset = with(density) {
                                    (selectedTextCenterShiftPx - ((1f - selectionProgress) * exitShiftPx)).toDp()
                                }

                                Box(
                                    modifier = Modifier
                                        .width(tabWidth)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) { onItemClick(item.id) }
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = contentColor,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .offset(x = iconOffset)
                                            .size(18.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .offset(x = textCenterOffset),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = isSelected,
                                            enter = fadeIn(
                                                animationSpec = tween(durationMillis = 240, delayMillis = 35, easing = textEasing)
                                            ) + slideInHorizontally(
                                                initialOffsetX = { it / 2 },
                                                animationSpec = tween(durationMillis = 320, easing = indicatorEasing)
                                            ),
                                            exit = fadeOut(
                                                animationSpec = tween(durationMillis = 170, easing = textEasing)
                                            ) + slideOutHorizontally(
                                                targetOffsetX = { -it / 2 },
                                                animationSpec = tween(durationMillis = 250, easing = iconEasing)
                                            )
                                        ) {
                                            Text(
                                                text = item.label,
                                                color = contentColor,
                                                style = textStyle,
                                                maxLines = 1
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
}

@Composable
private fun PrimarySectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (!actionText.isNullOrBlank() && onActionClick != null) {
            FilledTonalButton(
                onClick = onActionClick,
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(actionText, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun PersonalRecommendationRow(
    song: SongItem,
    onClick: () -> Unit
) {
    MediaListItemCard(
        title = song.title,
        subtitle = song.artist,
        onClick = onClick,
        leadingContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SongCover(
                    song = song,
                    modifier = Modifier.fillMaxSize(),
                    titleOverlay = false
                )
            }
        },
        trailingContent = {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LibrarySongRow(
    song: SongItem,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    MediaListItemCard(
        title = song.title,
        subtitle = "${song.artist} · ${formatDuration(song.durationMs)}",
        supportingText = "${song.formatLabel} 路 ${formatDuration(song.durationMs)}",
        onClick = onClick,
        leadingContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SongCover(
                    song = song,
                    modifier = Modifier.fillMaxSize(),
                    titleOverlay = false
                )
            }
        },
        trailingContent = {
            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) FavoriteRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}

@Composable
private fun LibraryPlaceholderPanel(title: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = when (title) {
                    "\u6587\u4ef6\u5939" -> Icons.Rounded.FolderOpen
                    "\u6b4c\u5355" -> Icons.Rounded.Star
                    else -> Icons.Rounded.MusicNote
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "\u5148\u628a\u89c6\u89c9\u5c42\u505a\u51fa\u6765\uff0c\u529f\u80fd\u7a0d\u540e\u518d\u63a5\u5165",
                style = MaterialTheme.typography.bodyMedium,
                color = SoftInk
            )
        }
    }
}

@Composable
private fun MineScreen(
    favoriteSongs: List<SongItem>,
    recentPlaySongs: List<SongItem>,
    savedFolderCount: Int,
    contentTopPadding: androidx.compose.ui.unit.Dp = 0.dp,
    isWideScreen: Boolean = false,
    useTopNavigationMode: Boolean = false,
    onOpenFavorites: () -> Unit,
    onRescan: () -> Unit,
    onManageFolders: () -> Unit,
    onPlaySong: (SongItem) -> Unit
) {
    val previewFavorites = favoriteSongs.take(4)
    val previewRecent = recentPlaySongs.take(4)
    val favoriteSongIds = remember(favoriteSongs) { favoriteSongs.map { it.id }.toSet() }

    SheetBounceContainer(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .bounceOverscroll(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 26.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier.height(contentTopPadding)
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = CardWhite,
                        tonalElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Box(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "\u6211\u7684",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "\u672c\u5730\u97f3\u4e50\u4e0e\u6536\u85cf\u7ba1\u7406",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SoftInk
                                    )
                                }
                                Surface(
                                    onClick = onRescan,
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Box(
                                        modifier = Modifier.size(36.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Sync,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SummaryCard(
                                    modifier = Modifier.weight(1f),
                                    title = "\u6536\u85cf",
                                    value = favoriteSongs.size.toString(),
                                    subtitle = "\u6211\u7684\u559c\u7231"
                                )
                                SummaryCard(
                                    modifier = Modifier.weight(1f),
                                    title = "\u6587\u4ef6\u5939",
                                    value = savedFolderCount.toString(),
                                    subtitle = "\u5df2\u8bb0\u4f4f"
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MineQuickActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Favorite,
                            text = "\u6211\u7684\u6536\u85cf",
                            onClick = onOpenFavorites
                        )
                        MineQuickActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Sync,
                            text = "\u91cd\u65b0\u626b\u63cf",
                            onClick = onRescan
                        )
                        MineQuickActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.FolderOpen,
                            text = "\u7ba1\u7406\u6587\u4ef6\u5939",
                            onClick = onManageFolders
                        )
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = CardWhite,
                        tonalElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SectionHeader(
                                title = "\u6211\u7684\u6536\u85cf",
                                actionText = "\u67e5\u770b\u5168\u90e8",
                                onActionClick = onOpenFavorites
                            )
                            if (previewFavorites.isEmpty()) {
                                Text(
                                    text = "\u8fd8\u6ca1\u6709\u6536\u85cf\u7684\u6b4c\u66f2",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoftInk
                                )
                            } else {
                                previewFavorites.forEach { song ->
                                    RecentPlayRow(
                                        song = song,
                                        isFavorite = true,
                                        onClick = { onPlaySong(song) },
                                        onToggleFavorite = {}
                                    )
                                }
                            }
                        }
                    }
                }

                if (previewRecent.isNotEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = CardWhite,
                            tonalElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SectionHeader(
                                    title = "\u6700\u8fd1\u542c\u8fc7",
                                    actionText = "${recentPlaySongs.size}\u9996"
                                )
                                previewRecent.forEach { song ->
                                    RecentPlayRow(
                                        song = song,
                                        isFavorite = song.id in favoriteSongIds,
                                        onClick = { onPlaySong(song) },
                                        onToggleFavorite = {}
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

@Composable
private fun MineQuickActionTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(68.dp),
        shape = RoundedCornerShape(18.dp),
        color = CardWhite,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
            Box(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyLibraryState(
    isScanning: Boolean,
    helperText: String?,
    onScanClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onScanClick,
                enabled = !isScanning,
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 34.dp, vertical = 16.dp)
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Box(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = if (isScanning) "\u626b\u63cf\u4e2d" else "\u626b\u63cf",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!helperText.isNullOrBlank()) {
                Box(modifier = Modifier.height(14.dp))
                Text(
                    text = helperText,
                    color = SoftInk,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        FilledTonalButton(
            onClick = { onActionClick?.invoke() },
            enabled = onActionClick != null,
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
            if (actionIcon != null) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Box(modifier = Modifier.width(6.dp))
            }
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun RecommendationCard(
    song: SongItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(256.dp)
            .aspectRatio(0.88f)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        SongCover(
            song = song,
            modifier = Modifier.fillMaxSize(),
            titleOverlay = true
        )
    }
}

@Composable
private fun RecentAddedCard(
    song: SongItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            SongCover(
                song = song,
                modifier = Modifier.fillMaxSize(),
                titleOverlay = false
            )
        }

        Box(modifier = Modifier.height(8.dp))

        Text(
            text = song.title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RecentPlayRow(
    song: SongItem,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Box(modifier = Modifier.height(6.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = SoftInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(modifier = Modifier.width(12.dp))

        Column(
            horizontalAlignment = Alignment.End
        ) {
            AssistChip(
                onClick = {},
                enabled = false,
                label = {
                    Text(song.formatLabel, fontSize = 10.sp)
                },
                colors = AssistChipDefaults.assistChipColors(
                    disabledContainerColor = Color.Transparent,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = false,
                    borderColor = MaterialTheme.colorScheme.outline,
                    disabledBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Box(modifier = Modifier.height(12.dp))
            Text(
                text = formatDuration(song.durationMs),
                style = MaterialTheme.typography.bodyLarge,
                color = SoftInk
            )
        }

        Box(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(74.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            SongCover(
                song = song,
                modifier = Modifier.fillMaxSize(),
                titleOverlay = false
            )

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) FavoriteRed else Color.White
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier,
    title: String,
    value: String,
    subtitle: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = CardWhite,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = SoftInk
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SoftInk
            )
        }
    }
}

@Composable
private fun SongCover(
    song: SongItem,
    modifier: Modifier,
    titleOverlay: Boolean
) {
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(song.albumArtUri)
                .crossfade(true)
                .build(),
            contentDescription = song.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (titleOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color(0xA0000000)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = song.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    color = Color.White.copy(alpha = 0.86f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (song.albumArtUri == null) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.88f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(if (titleOverlay) 40.dp else 24.dp)
            )
        }
    }
}

@Composable
private fun SongCollectionScreen(
    title: String,
    songs: List<SongItem>,
    favoriteIds: Set<Long>,
    onBack: () -> Unit,
    onPlaySong: (SongItem) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filteredSongs = remember(songs, query) {
        if (query.isBlank()) {
            songs
        } else {
            songs.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                SecondaryTopBar(
                    title = title,
                    onBack = onBack
                )
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 10.dp),
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                },
                placeholder = {
                    Text("\u641c\u7d22\u6b4c\u66f2\u6216\u6b4c\u624b")
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )

            if (filteredSongs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\u6682\u65e0\u5339\u914d\u6b4c\u66f2",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SoftInk
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .bounceOverscroll(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredSongs, key = { it.id }) { song ->
                        RecentPlayRow(
                            song = song,
                            isFavorite = song.id in favoriteIds,
                            onClick = { onPlaySong(song) },
                            onToggleFavorite = { onToggleFavorite(song.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderSelectionDialog(
    folders: List<FolderChoice>,
    onDismiss: () -> Unit,
    onFolderToggle: (String) -> Unit,
    onConfirm: () -> Unit
) {
    ActionSheet(
        title = "\u9009\u62e9\u4fdd\u7559\u8bfb\u53d6\u7684\u6587\u4ef6\u5939",
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = "\u626b\u63cf\u5b8c\u6210\u3002\u8bf7\u52fe\u9009\u9700\u8981\u957f\u671f\u4fdd\u7559\u8bfb\u53d6\u7684\u97f3\u4e50\u6587\u4ef6\u5939\u3002",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(modifier = Modifier.height(16.dp))
            SheetBounceContainer(
                modifier = Modifier.height(280.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(folders, key = { it.path }) { folder ->
                        FolderSelectionItem(
                            folder = folder,
                            onToggle = { onFolderToggle(folder.path) }
                        )
                    }
                }
            }
            Box(modifier = Modifier.height(8.dp))
            ActionSheetButton(
                text = "\u786e\u8ba4",
                onClick = onConfirm,
                enabled = folders.any { it.selected }
            )
        }
    }
}

@Composable
private fun FolderSelectionItem(
    folder: FolderChoice,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckPill(
            selected = folder.selected,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = folder.path,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${folder.songCount} \u9996\u6b4c",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun rememberPlaybackController(
    context: Context,
    onSongStarted: (SongItem) -> Unit
): LocalPlaybackController {
    val controller = remember(context) {
        LocalPlaybackController(context, onSongStarted)
    }

    DisposableEffect(controller) {
        onDispose {
            controller.release()
        }
    }

    return controller
}

private fun hasAudioPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_MEDIA_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
}

private fun hasNotificationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

private fun buildRecommendationSongs(
    songs: List<SongItem>,
    seed: Int
): List<SongItem> {
    if (songs.isEmpty()) return emptyList()
    val start = seed % songs.size
    return List(minOf(6, songs.size)) { index ->
        songs[(start + index) % songs.size]
    }
}

private fun songsFromIds(
    songs: List<SongItem>,
    ids: List<Long>
): List<SongItem> {
    val songMap = songs.associateBy { it.id }
    return ids.mapNotNull { songMap[it] }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun VibeMusAppPreview() {
    VibeMusTheme {
        VibeMusApp()
    }
}
