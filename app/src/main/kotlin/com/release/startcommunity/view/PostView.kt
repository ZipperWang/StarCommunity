package com.release.startcommunity.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.release.startcommunity.tool.Tool
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.SideEffect

import coil.request.ImageRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.release.startcommunity.model.User
import com.release.startcommunity.viewmodel.UserViewModel

import java.time.format.DateTimeFormatter

@Composable
fun PostListScreen(
    viewModel: PostViewModel = viewModel(factory = viewModelFactory { PostViewModel() }),
    onCreateClick: () -> Unit,
    onPostClick: (Post) -> Unit
) {
    val posts by viewModel.filteredPosts.collectAsState()
    val query by viewModel.query.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val hasLoadedAllData by viewModel.reachEnd.collectAsState()
    val listState = rememberLazyListState()

    // 修改预加载逻辑，只在未加载所有数据时触发
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty() && !hasLoadedAllData) {
                    val lastVisibleItemIndex = visibleItems.last().index
                    if (lastVisibleItemIndex >= posts.size - 5) {
                        viewModel.loadMore()
                    }
                }
            }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateClick) {
                Icon(Icons.Default.Add, contentDescription = "发帖")
            }
        }
    ) { innerPadding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
//                OutlinedTextField(
//                    value = query,
//                    onValueChange = { viewModel.updateSearchQuery(it) },
//                    placeholder = { Text("搜索帖子...") },
//                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
//                    singleLine = true,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 12.dp, vertical = 8.dp)
//                )
                M3SearchBar(query,{
                    viewModel.updateSearchQuery(it)
                })



                if (posts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无帖子，点击右下角按钮发布吧～")
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        reverseLayout = false,
                        userScrollEnabled = true
                    ) {
                        items(posts, key = { it.id }) { post ->
                            PostCard(post = post, onClick = { onPostClick(post) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun M3SearchBar(
    text: String,
    onTextChange: (String) -> Unit
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        leadingIcon = { Icon(Icons.Default.Search, null) },
        placeholder = { Text("搜索帖子...") },
        singleLine = true,
        shape = RoundedCornerShape(25.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor   = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor  = Color.White,
            focusedBorderColor      = Color.Transparent,
            unfocusedBorderColor    = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    )
}

@Composable
fun PostCard(post: Post, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // 简化用户信息行
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = post.author.avatar,
                    contentDescription = "头像",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(text = post.author.username, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(text = post.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))

            Text(text = post.content, maxLines = 3) // 限制行数以减少布局复杂度

            if (!post.images.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                AsyncImage(
                    model = post.images.first(), // 只显示第一张图
                    contentDescription = "帖子图片",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp) // 减小图片高度一减少渲染开销
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = null)
                Text(text = "${post.likes}")
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null)
                Text(text = "${post.comments.size}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    post: Post,
    onBack: () -> Unit,
) {
    val ui = rememberSystemUiController()
    val barColor = Color.White
    SideEffect { ui.setStatusBarColor(barColor, darkIcons = true) }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("测试") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp),
        ) {
            /** ---------- 作者信息 ---------- **/
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.author.avatar)
                        .crossfade(true)
                        .build(),
                    contentDescription = "avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = post.author.username,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = post.timestamp.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp,
            )


            if (!post.images.isNullOrEmpty()) {
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(post.images.size) { index ->
                        AsyncImage(
                            model = post.images[index],
                            contentDescription = "post image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(12.dp)),
                        )
                    }
                }
            }

            /** ---------- 点赞 / 评论 ---------- **/
            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                InteractionItem(
                    icon = Icons.Rounded.FavoriteBorder,
                    count = post.likes,
                )
                InteractionItem(
                    icon = Icons.Rounded.ChatBubbleOutline,
                    count = post.comments.size,
                )
            }

            /** ---------- 评论列表 ---------- **/
            if (post.comments.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "评论 (${post.comments.size})",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(8.dp))
                post.comments.forEach { comment ->
                    CommentCard(commenter = comment.author.username, text = comment.content)
                }
            }
        }
    }
}

/* ----------------- 可复用小组件 ----------------- */

@Composable
private fun InteractionItem(icon: ImageVector, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(4.dp))
        Text(count.toString(), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CommentCard(commenter: String, text: String) {
    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(commenter, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
    Spacer(Modifier.height(8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCreateScreen(
    onSubmit: (Post) -> Unit,
    onBack: () -> Unit,
    userViewModel: UserViewModel
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
                        author = User(
                            email = userViewModel.currentUser.value?.email ?: "",
                            username = userViewModel.currentUser.value?.username ?: "",
                            password = userViewModel.currentUser.value?.password ?: "",
                            avatar = userViewModel.currentUser.value?.avatar ?: ""),
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
