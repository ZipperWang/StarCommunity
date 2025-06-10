package com.release.startcommunity.model

import java.util.UUID

data class Comment (
    val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val author: User,
    val content: String,
    val likes: Int = 0,
    val avatar: String = "https://picsum.photos/200/300",
    val timestamp: String
)