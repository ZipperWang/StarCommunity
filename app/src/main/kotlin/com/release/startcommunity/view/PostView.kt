package com.release.startcommunity.view

import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.zIndex
import coil.request.ImageRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.release.startcommunity.api.CreatePostRequest
import com.release.startcommunity.viewmodel.UserViewModel


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
    if (showCreatePage){
        BackHandler {
            showCreatePage = false
        }
    }
    var showRichTextEditor by remember { mutableStateOf(false) }
    if (showRichTextEditor){
        BackHandler {
            showRichTextEditor = false
        }
    }


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
                            items(posts, key = { it.id  }) { post ->
                                PostCard(post = post, onClick = { onPostClick(post) }, postViewModel = viewModel, userViewModel)
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
                var title by remember { mutableStateOf("") }
                var content by remember { mutableStateOf("") }
                Box(modifier = Modifier.fillMaxSize()) {
                    PostCreateScreen(
                        onSubmit = {
                            viewModel.addPost(it.title, it.content, userViewModel.id.value)
                            showCreatePage = false
                        },
                        onBack = { showCreatePage = false },
                        userViewModel = userViewModel,
                        onRichTextEditor = { showRichTextEditor = true },
                        title = title,
                        onTitle = {title = it},
                        content = content,
                        onContent = {content = it},
                    )
                    AnimatedVisibility(
                        visible = showRichTextEditor,
                        enter = slideInVertically(
                            initialOffsetY = { it }, // 从底部滑入
                            animationSpec = tween(300)
                        ) + fadeIn(),
                        exit = slideOutVertically(
                            targetOffsetY = { it }, // 向底部滑出
                            animationSpec = tween(200)
                        ) + fadeOut(),
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(1f)
                    ) {
                        RichTextEditorScreen(
                            initTitle = title,
                            initContent = content,
                            onSubmit = {
                                content += it
                                showRichTextEditor = false
                            },
                            onBack = {
                                showRichTextEditor = false
                            }
                        )
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PostCard(post: Post,
             onClick: () -> Unit,
             postViewModel: PostViewModel,
             userViewModel: UserViewModel
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
                    val  avatarUrl = remember(post.user.avatar) { post.user.avatar }
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

                    Text(text = post.user.username, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(text = post.title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                RichText() {
                    Markdown(Tool.truncateText(post.content, 100))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.clickable {
                            postViewModel.likePost(post.id, userViewModel.id.value)
                        })
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
    onSubmitComment: (Long, String) -> Unit
) {

    LaunchedEffect(post.comments) {
        Log.d("PostDetailScreen更新了", "comments: ${post.comments}")
    }
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
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        // 评论栏
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
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
                        .padding(horizontal = 8.dp),
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
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        onSubmitComment(post.id, commentText)
                        commentText = ""
                    },
                    enabled = commentText.isNotBlank(),
                    modifier = Modifier.align(Alignment.Bottom),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("发送")
                }

            }
//            AnimatedVisibility(
//                visible = showCommentBar ,
//                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
//                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
//            ) {
//            Column(
//                modifier = Modifier
//                    .background(MaterialTheme.colorScheme.background)
//                    .padding(8.dp)
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(MaterialTheme.colorScheme.background)
//                        .padding(8.dp),
//                    verticalAlignment = Alignment.Bottom
//                ) {
//                    OutlinedTextField(
//                        value = commentText,
//                        onValueChange = { commentText = it },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(1f)
//                            .heightIn(min = 56.dp, max = 120.dp)
//                            .padding(horizontal = 8.dp),
//                        placeholder = { Text("写下你的评论...") },
//                        maxLines = 4,
//                        shape = RoundedCornerShape(12.dp),
//                        singleLine = false,
//                        keyboardOptions = KeyboardOptions.Default.copy(
//                            imeAction = ImeAction.Default,
//                            keyboardType = KeyboardType.Text
//                        ),
//                        keyboardActions = KeyboardActions(),
//                        colors = OutlinedTextFieldDefaults.colors(
//                            focusedBorderColor = MaterialTheme.colorScheme.primary,
//                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
//                            cursorColor = MaterialTheme.colorScheme.primary,
//                            focusedPlaceholderColor = Color.Gray,
//                            unfocusedPlaceholderColor = Color.LightGray
//                        )
//                    )
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    Button(
//                        onClick = {
//                            onSubmitComment(post.id, commentText)
//                            commentText = ""
//                                  },
//                        enabled = commentText.isNotBlank(),
//                        modifier = Modifier.align(Alignment.Bottom),
//                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
//                        shape = RoundedCornerShape(12.dp)
//                    ) {
//                        Text("发送")
//                    }
//
//                }
//
//                }
//            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp), // 为底部评论栏预留空间
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 作者信息
            item {


                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(post.user.avatar)
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
                            text = post.user.username,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = Tool.run { yieldPostTime(countGapFromNow(post.timestamp)) },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }

            // 正文内容
            item {
                RichText(
                    modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
                    Markdown(post.content)
                }
            }

            // 点赞评论行
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    InteractionItem(
                        icon = Icons.Rounded.FavoriteBorder,
                        count = post.likes,
                    )
                    // ... 其他按钮
                }
            }

            // 评论标题
            if (post.comments.isNotEmpty()) {
                item {
                    Text(
                        text = "评论 (${post.comments.size})",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                items(post.comments, key = { it.id }) { comment ->
                    CommentCard(commenter = comment.user.username, text = comment.content)
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
    onSubmit: (CreatePostRequest) -> Unit,
    onBack: () -> Unit,
    userViewModel: UserViewModel,
    onRichTextEditor: () -> Unit,
    title: String,
    onTitle: (String) -> Unit,
    content: String,
    onContent: (String) -> Unit,
) {

//    if (insertValueContent != null) {
//        content = insertValueContent
//    }
//
//    val previousBackStackEntry = navController.previousBackStackEntry
//    LaunchedEffect(previousBackStackEntry) {
//        val result = navController
//            .previousBackStackEntry
//            ?.savedStateHandle
//            ?.get<String>("editedContent")
//        result?.let {
//            content = it
//            navController.previousBackStackEntry?.savedStateHandle?.remove<String>("editedContent")
//        }
//    }

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
                onValueChange = onTitle,
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = content,
                onValueChange = onContent,
                label = { Text("内容") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRichTextEditor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("富文本编辑")
            }
            Button(
                onClick = {
                    val post = CreatePostRequest(title, content, userViewModel.id.value)
                    onSubmit(post)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("发布")
            }
        }
    }
}