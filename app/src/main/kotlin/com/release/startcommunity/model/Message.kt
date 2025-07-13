package com.release.startcommunity.model

data class Message (
    val id: Long,
    val chatId: Long,
    val senderId: Long,
    val content: String,
    val timestamp: String,
    val isRead: Boolean
)