package com.release.startcommunity


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import com.release.startcommunity.model.User
import com.release.startcommunity.view.RegisterScreen
import com.release.startcommunity.viewmodel.UserViewModel

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val userViewModel: UserViewModel by viewModels()
                RegisterScreen(
                    onRegisterSuccess = { user, code ->
                        Log.d("RegisterActivity", "onRegisterSuccess: $user")
                        userViewModel.registerUser(user, code)
                        finish()                        // 注册完直接关闭回到登录
                    },
                    onSendCode = { email ->
                        Log.d("RegisterActivity", "onSendCode: $email")
                        userViewModel.sendCode(email)
                        Log.d("RegisterActivity", "onSendCode: $email")
                    },
                    onLoginClick = { finish() }       // 顶部返回键或“已有账号？登录”
                    //TODO 未知bug，待修复
                )
            }
        }
    }
}