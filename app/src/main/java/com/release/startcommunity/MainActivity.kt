package com.release.startcommunity


import android.annotation.SuppressLint
import android.os.Bundle
import android.service.autofill.OnClickAction
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.core.view.WindowCompat
import com.release.startcommunity.model.Post
import com.release.startcommunity.view.PostCreateScreen
import com.release.startcommunity.view.PostDetailScreen



class MainActivity : ComponentActivity() {
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




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Application(){
    val userViewModel: UserViewModel = viewModel()
    val postsViewModel: PostViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(0) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var createPost by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (selectedPost == null && !createPost) {
                TopAppBar(title = {
                    Text(if (selectedTab == 0) "社区" else "我的")
                })
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("首页") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("我的") }
                )
            }
        }
    ) {padding ->
        AnimatedContent(
            targetState = selectedTab,
            label = "TabContent",
            modifier = Modifier.padding(padding)
        ) { target ->
            when (target) {
                0 -> {
                    when {
                        selectedPost != null -> PostDetailScreen(
                            post = selectedPost!!,
                            onBack = { selectedPost = null }
                        )
                        createPost -> PostCreateScreen(
                            onSubmit = {
                                postsViewModel.addPost(it)
                                createPost = false
                            },
                            onBack = { createPost = false }
                        )
                        else -> PostListScreen(
                            viewModel = postsViewModel,
                            onCreateClick = { createPost = true }, // 传入发帖操作
                            onPostClick = { selectedPost = it }
                        )
                    }
                }
                1 -> LoginScreen(onLogin = {
                    u, p -> userViewModel.login(u, p)
                })
            }
        }
    }
}


