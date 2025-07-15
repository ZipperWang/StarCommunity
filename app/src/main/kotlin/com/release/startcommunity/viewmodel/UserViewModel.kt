package com.release.startcommunity.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.release.startcommunity.SecureStore
import com.release.startcommunity.api.ApiClient
import com.release.startcommunity.api.LoginRequest
import com.release.startcommunity.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.release.startcommunity.api.RegisterRequest
import com.release.startcommunity.model.Event


class UserViewModel(private val app: Application): AndroidViewModel(app) {

    private val _loggedIn = MutableStateFlow<Boolean>(false)
    val loggedIn: StateFlow<Boolean> = _loggedIn

    private val _toastMessage = MutableLiveData<Event<String>>()        //Toast动态数据
    val toastMessage: LiveData<Event<String>> = _toastMessage


        init{
            tryAutoLogin()
        }


    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _id = MutableStateFlow<Long>(-1L)
    val id: StateFlow<Long> = _id

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchAllUsers() {
        viewModelScope.launch {
            try {
                _users.value = ApiClient.api.getUsers()
            } catch (e: Exception) {
                _errorMessage.value = "用户加载失败: ${e.message}"

            }
        }
    }

    fun fetchUserById(id: Long) {
        viewModelScope.launch {
            try {
                _currentUser.value = ApiClient.api.getUserById(id)
            } catch (e: Exception) {
                _errorMessage.value = "用户加载失败: ${e.message}"
            }
        }
    }

    private fun tryAutoLogin() = viewModelScope.launch {
        if (_loggedIn.value) return@launch            // 已有 token → 略过

        SecureStore.load(app)?.let { (user, pwd) ->
            runCatching {
                val res = ApiClient.api.login(LoginRequest(user, pwd))
                _id.value = res.userId
                _currentUser.value = ApiClient.api.getUserById(res.userId)
                _errorMessage.value = null
                _loggedIn.value = true
                SecureStore.save(app, user, pwd)  // 成功写 token → loggedIn=true
            }.onFailure { SecureStore.clear(app) }   // 失败清除凭据
        }
    }

    fun loginUser(username: String, password: String, sessionViewModel: SessionViewModel) {
        viewModelScope.launch {
            try {
                val res = ApiClient.api.login(LoginRequest(username, password))
                Log.d("Login", "登录成功:" + res.userId + res.token)
                _id.value = res.userId
                _currentUser.value = ApiClient.api.getUserById(res.userId)
                _errorMessage.value = null
                _loggedIn.value = true
                sessionViewModel.updateUserId(res.userId)
                SecureStore.save(app, username, password)
                _toastMessage.postValue(Event("登陆成功！"))
            }catch (e: Exception){
                _errorMessage.value = "登录失败: ${e.message}"
                _toastMessage.postValue(Event("登陆失败！"))
            }

        }
    }


    fun registerUser(user: RegisterRequest, code: String) {
        viewModelScope.launch {
            try {
                val res = ApiClient.api.verifyCode(user.email, code)
                Log.d("Register", "验证码验证开始")
                if (res.code() != 200) {
                    _errorMessage.value = "验证码验证失败"
                }else{
                    Log.d("Register", "验证码验证成功")
                    try {
                        val newUser = ApiClient.api.registerUser(user)
                        _currentUser.value = newUser.body()
                        _toastMessage.postValue(Event("注册成功！"))
                    }catch (e: Exception){
                        Log.e("Register", "注册失败[内]: ${e.message}")
                        _toastMessage.postValue(Event("注册失败！${e.message}"))
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "注册失败: ${e.message}"
                Log.e("Register", "注册失败: ${e.message}")
                _toastMessage.postValue(Event("注册失败！${e.message}"))
            }
        }
    }

    fun sendCode(email: String) {
        viewModelScope.launch {
            try {
                val res = ApiClient.api.sendCode(email)
                _errorMessage.value = null
                _toastMessage.postValue(Event("验证码发送成功！"))
            } catch (e: Exception) {
                _errorMessage.value = "发送验证码失败: ${e.message}"
            }
        }
    }

    fun verifyCode(email: String, code: String) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                _toastMessage.postValue(Event("验证码验证成功！"))
            } catch (e: Exception) {
                _errorMessage.value = "验证码验证失败: ${e.message}"
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            try {
                _id.value = -1L
                _currentUser.value = null
                _loggedIn.value = false
                SecureStore.clear(app)
                _toastMessage.postValue(Event("登出成功！"))
            } catch (e: Exception) {
                _errorMessage.value = "登出失败: ${e.message}"
                _toastMessage.postValue(Event("登出失败！${e.message}"))
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun uploadUserAvatar(userId: Long, uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val part = prepareAvatarPart(uri, context)
                val response = ApiClient.api.uploadAvatar(userId, part)
                if (response.isSuccessful) {
                    val avatarUrl = response.body()?.avatarUrl
                    _currentUser.value = _currentUser.value?.copy(avatar = avatarUrl.toString())
                    _toastMessage.postValue(Event("更改头像成功！"))
                } else {
                    Log.e("Upload", "上传失败: ${response.code()}")
                    _toastMessage.postValue(Event("更改头像失败！"))
                }
            } catch (e: Exception) {
                Log.e("Upload", "异常: ${e.message}")
                _toastMessage.postValue(Event("更改头像发生异常${e.message}"))
            }
        }
    }

    fun resizeImageTo100x100(context: Context, uri: Uri): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)

        val resizedBitmap = originalBitmap.scale(128, 128)

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

        return outputStream.toByteArray()
    }

    fun prepareAvatarPart(uri: Uri, context: Context): MultipartBody.Part {
//        val contentResolver = context.contentResolver
//        val inputStream = contentResolver.openInputStream(uri)!!
        val bytes = resizeImageTo100x100(context, uri)
        val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", "avatar.jpg", requestBody)
    }
}


