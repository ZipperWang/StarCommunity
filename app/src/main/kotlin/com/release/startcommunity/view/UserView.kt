package com.release.startcommunity.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.background
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.release.startcommunity.R
import com.release.startcommunity.api.RegisterRequest
import com.release.startcommunity.model.User
import com.release.startcommunity.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(onLogin: (String, String) -> Unit, onRegisterClick: () -> Unit) {
    var username by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ShaderBackground(modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("用户登录", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            M3InputTextBar(outText = username, hintText = "用户名", onTextChange = {
                username = it
            }, icon = Icons.Default.AccountCircle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp))
//        OutlinedTextField(
//            value = username,
//            onValueChange = { username = it },
//            label = { Text("用户名") })
            Spacer(Modifier.height(8.dp))
//        OutlinedTextField(
//            value = password,
//            onValueChange = { password = it },
//            label = { Text("密码") },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
//        )
            M3InputTextBar(outText = password, hintText = "密码", onTextChange = {
                password = it
            }, icon = Icons.Default.Password,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp))
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onLogin(username, password) },
                Modifier.fillMaxWidth()
                    .padding(12.dp)
                    .height(56.dp)
            ) {
                Text("登录")
            }
            Spacer(Modifier.height(96.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("没有账号？")

                TextButton(
                    onClick = onRegisterClick,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                ) {
                    Text("注册")
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: (user: RegisterRequest, code: String) -> Unit,
    onSendCode: (email: String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    /* --------------- UI 状态 --------------- */
    var email by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pwd  by remember { mutableStateOf("") }
    var pwd2 by remember { mutableStateOf("") }
    var pwdVisible by remember { mutableStateOf(false) }
    var timer by remember { mutableStateOf(0) }
    var code by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    /* --------------- 校验规则 --------------- */
    val emailValid = remember(email) { email.matches(Regex("\\w+@\\w+\\.\\w+")) }
    val pwdMatch   = remember(pwd, pwd2) { pwd.isNotEmpty() && pwd == pwd2 }
    val allValid   = emailValid && user.isNotBlank() && pwdMatch

    /* --------------- 页面结构 --------------- */
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("注册账户") })
        }
    ) { padding ->
        Column(
            modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            /* 账号 / 邮箱 ---------------------- */
            OutlinedTextField(
                value = user,
                onValueChange = { user = it },
                label = { Text("用户名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("邮箱") },
                isError = email.isNotEmpty() && !emailValid,
                supportingText = { if (!emailValid && email.isNotEmpty()) Text("邮箱格式不正确") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            /* 密码 / 确认密码 ------------------ */
            val pwVisual = if (pwdVisible) VisualTransformation.None else PasswordVisualTransformation()
            val toggleIcon = if (pwdVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
            OutlinedTextField(
                value = pwd,
                onValueChange = { pwd = it },
                label = { Text("密码") },
                visualTransformation = pwVisual,
                trailingIcon = {
                    IconButton({ pwdVisible = !pwdVisible }) { Icon(toggleIcon, null) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pwd2,
                onValueChange = { pwd2 = it },
                label = { Text("确认密码") },
                isError = pwd2.isNotEmpty() && !pwdMatch,
                supportingText = { if (!pwdMatch && pwd2.isNotEmpty()) Text("两次输入不一致") },
                visualTransformation = pwVisual,
                trailingIcon = {
                    IconButton({ pwdVisible = !pwdVisible }) { Icon(toggleIcon, null) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("验证码") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (email.isNotBlank()) {
                            onSendCode(email)

                            timer = 60
                            coroutineScope.launch {
                                while (timer > 0) {
                                    delay(1000)
                                    timer--
                                }
                            }
                        }
                    },
                    enabled = (timer == 0) && allValid
                ) {
                    Text(if (timer == 0 && allValid) "发送验证码" else if (!allValid)"格式错误"  else "重新发送($timer)")
                }
            }


            /* 注册按钮 ------------------------- */
            Button(
                onClick = { onRegisterSuccess(RegisterRequest(user, pwd, email), code) },
                enabled = allValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("注册")
            }

            /* 底部跳转行 ----------------------- */
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("已有账号？")
                TextButton(
                    onClick = onLoginClick,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                ) { Text("登录") }
            }
        }
    }
}

@Composable
fun M3InputTextBar(
    outText: String,
    hintText: String,
    onTextChange: (String) -> Unit,
    icon:ImageVector,
    modifier: Modifier
) {
    OutlinedTextField(
        value = outText,
        onValueChange = onTextChange,
        leadingIcon = { Icon(icon, null) },
        placeholder = { Text(hintText) },
        singleLine = true,
        shape = RoundedCornerShape(25.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor   = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor  = Color.White,
            focusedBorderColor      = Color.Transparent,
            unfocusedBorderColor    = Color.Transparent
        ),
        modifier = modifier
    )
}

/* ─────────  页面入口  ───────── */


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    currentTab: Int = 2,
    onTabChange: (Int) -> Unit,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    userViewModel: UserViewModel,
    onUserClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("") },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ))
        },
        containerColor = Color.Transparent,
    ) { inner ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ShaderBackground(modifier = Modifier.fillMaxSize())
            LazyColumn(
                contentPadding = PaddingValues(
                    top = inner.calculateTopPadding(),
                    bottom = inner.calculateBottomPadding() + 80.dp  // 为“退出”预留空间
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                item {
                    SectionCard() {
                        ProfileRow(
                            avatar = userViewModel.currentUser.value?.avatar
                                ?: "https://picsum.photos/200/300",
                            title = userViewModel.currentUser.value?.username ?: "默认名称",
                            subtitle = "UID@" + userViewModel.id.value,
                            userViewModel,
                            onClick = onUserClick
                        )
                    }
                }

                item { SectionTitle("应用") }
                item { SectionCard { SimpleRow("StarCommunity", "简体中文") } }


                item { SectionTitle("讨论") }
                item {
                    SectionCard {
                        SimpleRow("Telegram 群组")
                        HorizontalDivider(
                            Modifier.padding(horizontal = 16.dp),
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )
                        SimpleRow("Telegram 频道")
                    }
                }


                item { SectionTitle("其他") }
                item {
                    SectionCard {
                        SimpleRow("项目地址", "AGPL-3.0 开源")
                        HorizontalDivider(
                            Modifier.padding(horizontal = 16.dp),
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )
                        SimpleRow("官方网站")
                        HorizontalDivider(
                            Modifier.padding(horizontal = 16.dp),
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )
                        SimpleRow("引用")
                    }
                }
                item { SectionTitle("") }
                item { SectionTitle("") }
                item {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center) {
                        Button(
                            onClick = onLogout,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(76.dp)
                                .padding(bottom = 24.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("退出登录", color = Color.White)
                        }
                    }
                }
            }



        }
    }
}

/* ───────── 可复用组件，下方与前示例相同 ───────── */

@Composable
private fun SectionTitle(text: String) =
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 24.dp, bottom = 4.dp)
    )

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) =
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) { Column(content = content) }




@Composable
private fun ProfileRow(
    avatar: String,
    title: String,
    subtitle: String,
    viewModel: UserViewModel,
    onClick: () -> Unit
) {
    RowItem(onClick = {
        onClick()
    }) {
        val context = LocalContext.current
        val imageUri = remember { mutableStateOf<Uri?>(null) }

        // 图片选择 launcher
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let { imageUri.value = it }
        }

        // 权限 launcher
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE

        val permissionGranted = remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            permissionGranted.value = granted
            if (granted) {
                imagePickerLauncher.launch("image/*")
            }
        }


        LaunchedEffect(imageUri.value) {
            imageUri.value?.let {
                viewModel.uploadUserAvatar(viewModel.id.value, it, context)
            }
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatar)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .clickable {
                    if (permissionGranted.value) {
                        imagePickerLauncher.launch("image/*")
                    } else {
                        permissionLauncher.launch(permission)
                    }
                },
            contentScale = ContentScale.Crop
        )
//    AsyncImage(
//        model = ImageRequest.Builder(LocalContext.current)
//            .data(post.author.avatar)
//            .crossfade(true)
//            .build(),
//        contentDescription = "avatar",
//        modifier = Modifier
//            .size(48.dp)
//            .clip(CircleShape),
//        contentScale = ContentScale.Crop,
//    )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SimpleRow(title: String, subtitle: String? = null) = RowItem( onClick = {}) {
    Column(Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        subtitle?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RowItem(onClick: () -> Unit, content: @Composable RowScope.() -> Unit) = Row(
    modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }                        // TODO: 挂接跳转逻辑
        .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    content = {
        content()
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
)


