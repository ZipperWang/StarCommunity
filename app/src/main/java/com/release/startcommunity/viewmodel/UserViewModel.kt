package com.release.startcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class UserViewModel : ViewModel() {
    private val _isAuthenticated = mutableStateOf(false)
    val isAuthenticated: State<Boolean> = _isAuthenticated

    fun login(username: String, password: String) {
        _isAuthenticated.value = username.isNotEmpty() && password.isNotEmpty()
    }

    fun logout() {
        _isAuthenticated.value = false
    }

}


