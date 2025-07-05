package com.release.startcommunity.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.LocalIndication
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.text.input.KeyboardType
import coil.request.ImageRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.release.startcommunity.model.User
import com.release.startcommunity.viewmodel.UserViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PostListScreen(
    viewModel: PostViewModel = viewModel(factory = viewModelFactory { PostViewModel() }),
    userViewModel: UserViewModel,
    onCreateClick: () -> Unit,
    onPostClick: (Post) -> Unit,
) {
    val posts by viewModel.filteredPosts.collectAsState()
    val query by viewModel.query.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val hasLoadedAllData by viewModel.reachEnd.collectAsState()
    val listState = rememberLazyListState()
    var showCreatePage by remember { mutableStateOf(false) }


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
            if (!showCreatePage) {
                FloatingActionButton(
                    onClick = {
                        showCreatePage = true
                    },
                ) {
                    Icon(Icons.Default.Add, contentDescription = "发帖")
                }
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
                    M3SearchBar(query, {
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
            // 发布界面动画展开层
            AnimatedVisibility(
                visible = showCreatePage,
                enter = fadeIn(animationSpec = tween(500)) + expandIn(
                    expandFrom = Alignment.BottomEnd,
                    animationSpec = tween(500)
                ),
                exit = fadeOut(animationSpec = tween(500)) + shrinkOut(
                    shrinkTowards = Alignment.BottomEnd,
                    animationSpec = tween(500)
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                PostCreateScreen(
                    onSubmit = {
                        viewModel.addPost(it.title, it.content, userViewModel.id.value)
                        showCreatePage = false
                    },
                    onBack = { showCreatePage = false },
                    userViewModel = userViewModel
                )

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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PostCard(post: Post,
             onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                // 简化用户信息行
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val  avatarUrl = remember(post.author.avatar) { post.author.avatar }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(avatarUrl)
                            .crossfade(true)
                            .build(),
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PostDetailScreen(
    post: Post,
    onBack: () -> Unit,
    showCommentBar: Boolean,
    onSubmitComment: () -> Unit
) {
    val ui = rememberSystemUiController()
    val barColor = Color.White
    var commentText by remember { mutableStateOf("") }
    SideEffect { ui.setStatusBarColor(barColor, darkIcons = true) }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(post.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        // 评论栏
        bottomBar = {
            AnimatedVisibility(
                visible = showCommentBar ,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
            Column(
                modifier = Modifier
                    .background(Color(0xFFF8F8F8))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFDFDFD))
                        .padding(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .heightIn(min = 56.dp, max = 120.dp)
                            .padding(horizontal = 4.dp),
                        placeholder = { Text("写下你的评论...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        singleLine = false,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Default,
                            keyboardType = KeyboardType.Text
                        ),
                        keyboardActions = KeyboardActions(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onSubmitComment,
                        enabled = commentText.isNotBlank(),
                        modifier = Modifier.align(Alignment.Bottom),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("发送")
                    }
                }

                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
        ) {
            /** ---------- 作者信息 ---------- **/
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post. author.avatar)
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
