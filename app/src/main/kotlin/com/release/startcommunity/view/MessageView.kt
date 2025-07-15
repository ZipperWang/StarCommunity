package com.release.startcommunity.view



import android.util.Log
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.text.style.TextOverflow
import com.release.startcommunity.api.CreateMessageRequest
import com.release.startcommunity.model.ChatSessionSummary
import com.release.startcommunity.viewmodel.ChatViewModel
import com.release.startcommunity.viewmodel.SessionViewModel
import com.release.startcommunity.viewmodel.UserViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageListScreen(
    sessionList: List<ChatSessionSummary>,
    onSessionClick: (session: ChatSessionSummary) -> Unit,
    sessionViewModel: SessionViewModel,
    userViewModel: UserViewModel
) {
    LaunchedEffect(Unit) {
        sessionViewModel.loadSessions()
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("私信列表") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(sessionList) { session ->
                SessionItem(session = session) {
                    onSessionClick(session)
                }
                Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }
        }
    }
}
@Composable
fun SessionItem(session: ChatSessionSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = session.friendAvatarUrl,
            contentDescription = "头像",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.friendNickName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = Tool.run { yieldPostTime(countGapFromNow(session.lastTime)) },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = session.lastMessage,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    userId: Long,
    friendId: Long,
    friendAvatarUrl: String,
    viewModel: ChatViewModel = remember {
        ChatViewModel(userId, friendId)
    },
    onBack: () -> Unit
) {
    val messages = viewModel.messages
    var input by remember { mutableStateOf("") }
    DisposableEffect(Unit) {
        viewModel.connect()
        onDispose {
            viewModel.disconnect()
        }
    }

    Scaffold(
        topBar ={
            CenterAlignedTopAppBar(
                title = { Text(userId.toString()) },
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) { message ->
                    val isMe = message.senderId == userId
                    ChatMessageItem(message = message, isMe = isMe, avatarUrl = friendAvatarUrl)//
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 4.dp),
//                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .background(
//                                    if (isMe) MaterialTheme.colorScheme.primary
//                                    else MaterialTheme.colorScheme.secondary,
//                                    shape = RoundedCornerShape(12.dp)
//                                )
//                                .padding(10.dp)
//                        ) {
//                            Text(
//                                text = message.content,
//                                color = if (isMe) Color.White else Color.Black,
//                                fontSize = 16.sp
//                            )
//                        }
//                    }
                }

            }
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入消息...") },
                    maxLines = 4,
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {
                    viewModel.sendMessage(input)
                    input = ""
                }) {
                    Text("发送")
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: CreateMessageRequest,
    isMe: Boolean,
    avatarUrl: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "对方头像",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .background(
                    color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                color = if (isMe) Color.White else Color.Black
            )
        }

        if (isMe) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}