package com.release.startcommunity


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.autofill.OnClickAction
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize

import android.graphics.Shader
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel

import com.release.startcommunity.ui.theme.StartCommunityTheme
import com.release.startcommunity.view.LoginScreen
import com.release.startcommunity.view.PostListScreen
import com.release.startcommunity.viewmodel.PostViewModel
import com.release.startcommunity.viewmodel.UserViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.release.startcommunity.model.Post
import com.release.startcommunity.model.User
import com.release.startcommunity.view.HyperOSBackground
import com.release.startcommunity.view.PostCreateScreen
import com.release.startcommunity.view.PostDetailScreen
import com.release.startcommunity.view.RegisterScreen
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.compose.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.release.startcommunity.view.AboutScreen


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
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
    }
}




@SuppressLint("StateFlowValueCalledInComposition")
@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Application(){
    val userViewModel: UserViewModel = viewModel()
    val postsViewModel: PostViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(0) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var createPost by remember { mutableStateOf(false) }
    val sysUi = rememberSystemUiController()
    LaunchedEffect(Unit) {
        sysUi.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = false,
            isNavigationBarContrastEnforced = false   // 关闭强制对比度，避免变灰
        )
    }
    val loggedIn by userViewModel.loggedIn.collectAsState()



    Scaffold(
//        topBar = {
//            if (selectedPost == null && !createPost) {
//                TopAppBar(title = {
//                    Text(if (selectedTab == 0) "社区" else "我的")
//                })
//            }
//        },
        bottomBar = {
//            NavigationBar (containerColor = Color.White) {
//                NavigationBarItem(
//                    selected = selectedTab == 0,
//                    onClick = { selectedTab = 0 },
//                    icon = { Icon(Icons.Default.Home, null) },
//                    label = { Text("首页") }
//                )
//                NavigationBarItem(
//                    selected = selectedTab == 1,
//                    onClick = { selectedTab = 1 },
//                    icon = { Icon(Icons.Default.Person, null) },
//                    label = { Text("我的") }
//                )
//            }
            GlassNavigationBar(selectedTab,
                onSelect = {
                selectedTab = it
            })
        },
        modifier = Modifier.navigationBarsPadding()
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
                    when {
                        selectedPost != null -> PostDetailScreen(
                            post = selectedPost!!,
                            onBack = { selectedPost = null }
                        )
                        createPost -> PostCreateScreen(
                            onSubmit = {
                                postsViewModel.addPost(it.title, it.content, userViewModel.id.value)
                                createPost = false
                            },
                            onBack = { createPost = false },
                            userViewModel = userViewModel
                        )
                        else -> PostListScreen(
                            viewModel = postsViewModel,
                            onCreateClick = { createPost = true }, // 传入发帖操作
                            onPostClick = { selectedPost = it }
                        )
                    }
                }
                1 ->  {
                    if (!loggedIn) {
                        HyperOSBackground {
                            LoginScreen(
                                onLogin = { username, password ->
                                    userViewModel.loginUser(username, password)
                                },
                                onRegisterClick = {
                                    ctx.startActivity(Intent(ctx, RegisterActivity::class.java))
                                }
                            )
                        }
                    }else{
                        AboutScreen(
                            onTabChange = { selectedTab = it },
                            onToggleTheme = {
                                // TODO: 切换主题
                            },
                            onLogout = {
                                userViewModel.logoutUser()
                            },
                            userViewModel = userViewModel
                        )
                    }
//                    HyperOSBackground {
//                        LoginScreen(
//                            onLogin = { username, password ->
//                                userViewModel.loginUser(username, password)
//                            },
//                            onRegisterClick = {
//
//                                ctx.startActivity(Intent(ctx, RegisterActivity::class.java))
//                            }
//                        )
//                    }

                }
            }
        }
    }
}

/* -----------------------------------------------------------
 * 玻璃效果底部导航栏（Compose + Material3）
 * ----------------------------------------------------------- */
@Composable
fun GlassNavigationBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    val items = listOf(
        Icons.Default.Home      to "首页",
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


