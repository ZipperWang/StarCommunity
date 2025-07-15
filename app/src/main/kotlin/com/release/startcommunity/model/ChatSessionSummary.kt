package com.release.startcommunity.model

data class ChatSessionSummary(
    val friendId: Long,
    val friendNickName: String,
    val friendAvatarUrl: String,
    val lastMessage: String,
    val lastTime: String
)