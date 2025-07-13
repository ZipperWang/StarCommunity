package com.release.startcommunity.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.release.startcommunity.api.ApiClient
import com.release.startcommunity.api.CreateMessageRequest
import com.release.startcommunity.model.Message
import kotlinx.coroutines.launch

class ChatViewModel(
    private val userId: Long,
    private val chatId: Long,
    private val friendId: Long
) : ViewModel() {

    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> = _messages

    var inputText by mutableStateOf("")

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            try {
                val result = ApiClient.api.getHistory(userA = userId, userB = friendId)
                _messages.clear()
                _messages.addAll(result.body()?:emptyList())
            } catch (e: Exception) {
                Log.e("Chat", "加载历史消息失败: ${e.message}")
            }
        }
    }

    fun sendMessage() {
        val content = inputText.trim()
        if (content.isEmpty()) return

        val request = CreateMessageRequest(chatId, userId, content)
        viewModelScope.launch {
            try {
                ApiClient.api.sendMessage(request)
                _messages.add(
                    Message(
                        id = -1,
                        chatId = chatId,
                        senderId = userId,
                        content = content,
                        timestamp = "",
                        isRead = true
                    )
                )
                _messages.add(
                    Message(
                        id = -1,
                        chatId = chatId,
                        senderId = userId + 1,
                        content = content,
                        timestamp = "",
                        isRead = true
                    )
                )
                inputText = ""
            } catch (e: Exception) {
                Log.e("Chat", "发送失败: ${e.message}")
            }
        }
    }
}