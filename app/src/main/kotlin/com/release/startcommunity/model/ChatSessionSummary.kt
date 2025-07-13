package com.release.startcommunity.model

data class ChatSessionSummary(
    val chatId: Long,
    val friendId: Long,
    val friendNickname: String,
    val friendAvatarUrl: String,
    val lastMessage: String,
    val lastTime: String
)