package com.release.startcommunity.viewmodel

import android.media.midi.MidiSender
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.release.startcommunity.api.ApiClient
import com.release.startcommunity.api.CreateMessageRequest
import com.release.startcommunity.api.CreateSessionRequest
import com.release.startcommunity.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject

class ChatViewModel(
    private val myUserId: Long,
    private val targetUserId: Long
) : ViewModel() {

    val messages = mutableStateListOf<CreateMessageRequest>()
    private var webSocket: WebSocket? = null

    private val client = OkHttpClient()
    private val _chatId = MutableStateFlow(0L)
    val chatId: StateFlow<Long> = _chatId

    init {
        getChatId()
        getHistory()
    }

    fun connect() {
        val request = Request.Builder()
            .url("ws://api.starcommunity.asia:54321/ws/chat?userId=$myUserId")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("ChatViewModel", "WebSocket 状态：已连接")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val from = json.optString("from")
                    val msg = json.optString("msg")
                    val time = json.optLong("time")
                    if (from == myUserId.toString()) addMessage(msg,from.toLong())
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "消息解析失败：${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("ChatViewModel", "连接失败：${t.message}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("ChatViewModel", "连接关闭：$reason")
            }
        })
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        val json = JSONObject()
        json.put("to", targetUserId.toString())
        json.put("msg", content)
        Log.d("ChatViewModel", "发送消息$messages")

        webSocket?.send(json.toString())

        addMessage(content, myUserId)
    }

    private fun addMessage(content: String, senderId: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            messages.add(CreateMessageRequest(
                chatId = chatId.value,
                senderId = senderId,
                content = content
            ))
        }
    }

    private fun getHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = ApiClient.api.getHistory(userA = myUserId, userB = targetUserId)
            if (response.isSuccessful) {
                val history = response.body()
                if (history != null) {
                    messages.addAll(history.map {
                        CreateMessageRequest(
                            chatId = it.chatId,
                            senderId = it.senderId,
                            content = it.content
                        )
                    })
                }
            }
        }
    }

    private fun getChatId() {
        viewModelScope.launch(Dispatchers.IO) {
            val response =ApiClient.api.newSession(CreateSessionRequest(myUserId, targetUserId))
            if (response.isSuccessful) {
                val session = response.body()
                if (session != null) {
                    _chatId.value = session
                }
            }
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "连结已断开")
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

}