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
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
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
import com.release.startcommunity.model.Comment
import com.release.startcommunity.model.User
import com.release.startcommunity.tool.Tool
import com.release.startcommunity.tool.Tool.Companion.formatTimestamp
import androidx.compose.foundation.lazy.rememberLazyListState

@Composable

fun PostListScreen(
    viewModel: PostViewModel = viewModel(factory = viewModelFactory { PostViewModel() }),
    onCreateClick: () -> Unit,
    onPostClick: (Post) -> Unit
) {
    val posts by viewModel.filteredPosts.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val listState = rememberLazyListState()

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
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("搜索帖子...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )

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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
fun PostCard(post: Post, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // 用户信息行
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

                Column {
                    Text(text = post.author.username, fontWeight = FontWeight.Bold)
                    Text(
                        text = formatTimestamp(post.timestamp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(text = post.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))

            Text(text = post.content, maxLines = 6)

            if (!post.images.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                AsyncImage(
                    model = post.images.first(), // 只显示第一张图（你可自定义轮播等）
                    contentDescription = "帖子图片",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
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
fun PostDetailScreen(post: Post, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("帖子内容") },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // 用户信息区
            UserInfo(user = post.author)
            Spacer(modifier = Modifier.height(8.dp))

            // 帖子内容区
            PostContent(post = post)
            Spacer(modifier = Modifier.height(8.dp))

            // 图片
            PostImage(imageUrl = post.images.toString())
            Spacer(modifier = Modifier.height(8.dp))

            // 点赞和评论
            PostInteraction(post = post)
            Spacer(modifier = Modifier.height(16.dp))

            // 评论区
            CommentSection(comments = post.comments)
        }
    }
}

@Composable
fun UserInfo(user: User) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = user.avatar,
            contentDescription = "User Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = user.username, fontWeight = FontWeight.Bold)
            //Text(text = user.bio, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun PostContent(post: Post) {
    Text(text = post.title, style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = post.content)
}

@Composable
fun PostImage(imageUrl: String) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Post Image",
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun PostInteraction(post: Post) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Favorite, contentDescription = "Like")
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = post.likes.toString())
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Comment, contentDescription = "Comment")
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = post.comments.size.toString())
        }
    }
}

@Composable
fun CommentSection(comments: List<Comment>) {
    LazyColumn {
        items(comments) { comment ->
            CommentItem(comment = comment)
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = comment.author.avatar,
            contentDescription = "Commenter Avatar",
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = comment.author.username, fontWeight = FontWeight.Bold)
            Text(text = comment.content, style = MaterialTheme.typography.bodySmall)
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
                        author = User(username = "release", password = "123456"), // 可根据登录用户替换
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
