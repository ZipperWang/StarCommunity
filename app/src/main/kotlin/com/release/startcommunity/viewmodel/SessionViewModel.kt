package com.release.startcommunity.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.release.startcommunity.api.ApiClient
import com.release.startcommunity.model.ChatSessionSummary
import kotlinx.coroutines.launch

class SessionViewModel() : ViewModel() {

    var chatSessions by mutableStateOf<List<ChatSessionSummary>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var userId by mutableLongStateOf(0L)
        private set


    fun loadSessions() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val sessions = ApiClient.api.getChatSessions(userId)
                chatSessions = sessions
            } catch (e: Exception) {
                errorMessage = "加载失败: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun updateUserId(id: Long) {
        userId = id
    }
}