package com.release.startcommunity.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.release.startcommunity.model.Post
import com.release.startcommunity.viewmodel.PostViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.release.startcommunity.tool.Tool

@Composable

fun PostListScreen(viewModel: PostViewModel = viewModel(factory = viewModelFactory { PostViewModel() }),
                   onCreateClick: () -> Unit, onPostClick: (Post) -> Unit) {
        val posts by viewModel.filteredPosts.collectAsState()  // 假设你已添加搜索支持
        val query by viewModel.searchQuery.collectAsState()

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = onCreateClick) {
                    Icon(Icons.Default.Add, contentDescription = "发帖")
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("搜索帖子...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(posts) { post ->
                        PostCard(post, onClick = { onPostClick(post) })
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

}

@Composable
fun PostCard(post: Post, onClick: () -> Unit) {
    Card( modifier = Modifier.fillMaxWidth()
        .clickable {
            onClick()
        }, elevation = CardDefaults.cardElevation(6.dp) )
    {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(post.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text(post.content, maxLines = 3)
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(post.author, style = MaterialTheme.typography.bodySmall)
                Text(post.timestamp, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(post: Post, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("帖子详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(post.title, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("作者：${post.author} • ${post.timestamp}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            Text(post.content, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCreateScreen(
    onSubmit: (Post) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发帖") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("内容") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val post = Post(
                        title = title,
                        content = content,
                        author = "你自己", // 可根据登录用户替换
                        timestamp = Tool.getCurrentTimestamp()
                    )
                    onSubmit(post)
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("发布")
            }
        }
    }
}
