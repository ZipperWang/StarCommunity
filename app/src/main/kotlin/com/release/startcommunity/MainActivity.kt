package com.release.startcommunity


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.release.startcommunity.api.ApiClient
import com.release.startcommunity.model.ChatSessionSummary
import com.release.startcommunity.model.Post
import com.release.startcommunity.ui.theme.StartCommunityTheme
import com.release.startcommunity.view.AboutScreen
import com.release.startcommunity.view.LoginScreen
import com.release.startcommunity.view.MessageListScreen
import com.release.startcommunity.view.MessageScreen
import com.release.startcommunity.view.PersonalProfileScreen
import com.release.startcommunity.view.PostDetailScreen
import com.release.startcommunity.view.PostListScreen
import com.release.startcommunity.view.ShaderBackground
import com.release.startcommunity.viewmodel.PostViewModel
import com.release.startcommunity.viewmodel.SessionViewModel
import com.release.startcommunity.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModelOfPostView: PostViewModel
    private lateinit var viewModelOfUserView: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        setContent {
            StartCommunityTheme(dynamicColor = false) {
                Application()
            }
        }

        viewModelOfPostView = ViewModelProvider(this)[PostViewModel::class.java]
        viewModelOfUserView = ViewModelProvider(this)[UserViewModel::class.java]
        setupObservers()

    }

    private fun setupObservers() {        //在Activity中创建Event监听器

        viewModelOfPostView.toastMessage.observe(this){        //监听Post-Toast-LiveData
            event -> event.getContentIfNotHandled()?.let { message ->  showToast(message)}
        }
        viewModelOfUserView.toastMessage.observe(this){        //监听User-Toast-LiveData
                event -> event.getContentIfNotHandled()?.let { message ->  showToast(message)}
        }
    }

    private fun showToast(message: String) {        //创建Toast
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}




@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun Application(){
    val userViewModel: UserViewModel = viewModel()
    val postsViewModel: PostViewModel = viewModel()
    val sessionViewModel: SessionViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(0) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var createPost by remember { mutableStateOf(false) }
    val selectedSession = remember { mutableStateOf<ChatSessionSummary?>(null) }
    var showDetails by remember { mutableStateOf(false) }
    var showChat by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    if (showDetails){
        BackHandler {
            showDetails = false
        }
    }
    if (showProfile){
        BackHandler {
            showProfile = false
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val sysUi = rememberSystemUiController()

    LaunchedEffect(Unit) {
        sysUi.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = false,
            isNavigationBarContrastEnforced = false
        )
    }
    val loggedIn by userViewModel.loggedIn.collectAsState()

    val bottomBarState = remember { MutableTransitionState(true) }
    bottomBarState.targetState = !(showDetails || showChat)



    val userId by userViewModel.id.collectAsState()
    LaunchedEffect(userId) {
        sessionViewModel.updateUserId(userId)
    }


    Scaffold(
        bottomBar = {
            GlassNavigationBar(
                selectedTab,
                onSelect = {
                    selectedTab = it
                })
        },
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxSize(),
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
                AnimatedContent(
                    targetState = selectedTab,
                    label = "TabContent",
                    modifier = Modifier.padding(padding)
                ) { target ->
                    val ctx = LocalContext.current
                    when (target) {
                        0 -> {
                            Box {
                                // 帖子列表
                                PostListScreen(
                                    viewModel = postsViewModel,
                                    onCreateClick = { createPost = true }, // 传入发帖操作
                                    onPostClick = {
                                        selectedPost = it
                                        showDetails = false
                                        coroutineScope.launch {
                                            val comments = ApiClient.api.getComments(it.id)
                                            selectedPost = it.copy(comments = comments)
                                            showDetails = true
                                            Log.d("PostVM", "加载评论成功$comments")
                                        }
                                    },
                                    userViewModel = userViewModel
                                )
                            }
                        }
                        1 -> {
                            Box(modifier = Modifier.fillMaxSize()){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ShaderBackground(modifier = Modifier.fillMaxSize())
                            }
                        }

                        2 -> {


                            val testData = sessionViewModel.chatSessions
                            if (showChat){
                                BackHandler {
                                    sessionViewModel.loadSessions()
                                    showChat = false
                                }
                            }
                           MessageListScreen(
                               sessionList = testData,
                               onSessionClick = { session ->
                                   selectedSession.value = session
                                   coroutineScope.launch {
                                       delay(50)
                                       showChat = true
                                   }
                               },
                               sessionViewModel = sessionViewModel,
                               userViewModel = userViewModel
                           )
//                            AnimatedVisibility(
//                                visible = showChat,
//                                enter = slideInHorizontally(
//                                    initialOffsetX = { it },
//                                    animationSpec = tween(350, easing = FastOutSlowInEasing)
//                                ) + fadeIn(tween(350)),
//                                exit = slideOutHorizontally(
//                                    targetOffsetX = { it },
//                                    animationSpec = tween(350, easing = FastOutSlowInEasing)
//                                ) + fadeOut(tween(350))
//                            ) {
//                                MessageScreen(
//                                    userId = userViewModel.id.value,
//                                    friendId = selectedSession.value?.friendId ?: 0,
//                                    onBack = {showChat = false},
//                                    friendAvatarUrl = selectedSession.value?.friendAvatarUrl ?: "",
//                                    showCommentBar = showCommentBar
//                                )
//                            }
                        }

                        3 -> {
                            if (!loggedIn) {
                                    LoginScreen(
                                        onLogin = { username, password ->
                                            userViewModel.loginUser(username, password, sessionViewModel)
                                        },
                                        onRegisterClick = {
                                            ctx.startActivity(
                                                Intent(
                                                    ctx,
                                                    RegisterActivity::class.java
                                                )
                                            )
                                        }
                                    )

                            } else {
                                // 用户信息
                                    AboutScreen(
                                        onTabChange = { selectedTab = it },
                                        onToggleTheme = {
                                            // TODO: 切换主题
                                        },
                                        onLogout = {
                                            userViewModel.logoutUser()
                                        },
                                        userViewModel = userViewModel,
                                        onUserClick = {
                                           showProfile =  true
                                        }
                                    )
                            }
                        }
                    }
                }
            }
    AnimatedVisibility(
        visible = selectedPost != null && showDetails,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(350)),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        ) + fadeOut(tween(350))
    ) {
        selectedPost?.let { post ->
            PostDetailScreen(
                post = post,
                onBack = { showDetails = false },
                showCommentBar = true,
                onSubmitComment = { postId, content ->
                    postsViewModel.createComment(
                        postId,
                        content,
                        userViewModel.currentUser.value,
                        onCommentCreated = { comment ->
                            selectedPost = post.copy(comments = post.comments + comment)
                        }
                    )
                }
            )
        }
    }
    AnimatedVisibility(
        visible = showChat,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(350)),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        ) + fadeOut(tween(350))
    ) {
        MessageScreen(
            userId = userViewModel.id.value,
            friendId = selectedSession.value?.friendId ?: 0,
            onBack = {showChat = false},
            friendAvatarUrl = selectedSession.value?.friendAvatarUrl ?: "",
            friendNickName = selectedSession.value?.friendNickName ?: "",
            showCommentBar = true
        )
    }
    AnimatedVisibility(
        visible = showProfile,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(350)),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        ) + fadeOut(tween(350))
    ) {
        PersonalProfileScreen()
    }
}



// 底部导航栏（）
@Composable
fun GlassNavigationBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    val items = listOf(
        Icons.Default.Home      to "首页",
        Icons.Default.Star      to "板块",
        Icons.Default.Message      to "消息",
        Icons.Default.Person    to "我的"
    )

    /* 背景：半透明渐变 +（API 31 以上）毛玻璃 */
    val blur = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.graphics.RenderEffect
                .createBlurEffect(28f, 28f, Shader.TileMode.CLAMP)
                .asComposeRenderEffect()
        } else null
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)                      // 高度含手势区
    ) {
        /* 背景层 */
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.White.copy(alpha = .55f),
                        1f to Color.White.copy(alpha = .35f)
                    )
                )
                .graphicsLayer { if (blur != null) renderEffect = blur }
        )

        /* 前景层：真正的导航按钮 */
        NavigationBar(
            tonalElevation = 0.dp,
            containerColor = Color.Transparent
        ) {
            items.forEachIndexed { idx, (icon, label) ->
                NavigationBarItem(
                    selected = idx == selectedIndex,
                    onClick  = { onSelect(idx) },
                    icon     = { Icon(icon, null) },
                    label    = { Text(label) },
                    alwaysShowLabel = false
                )
            }
        }
    }
}







