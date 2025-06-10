package com.release.startcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import com.release.startcommunity.api.ApiClient
import com.release.startcommunity.api.LoginRequest
import com.release.startcommunity.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

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

    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.login(LoginRequest(username, password))
                //TokenStore.save(response.token)
                _currentUser.value = ApiClient.api.getUserById(response.uid)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "登录失败: ${e.message}"
            }
        }
    }


    fun registerUser(user: User) {
        viewModelScope.launch {
            try {
                val newUser = ApiClient.api.registerUser(user)
                _currentUser.value = newUser
            } catch (e: Exception) {
                _errorMessage.value = "注册失败: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}


