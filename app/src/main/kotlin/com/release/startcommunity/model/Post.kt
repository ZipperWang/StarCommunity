package com.release.startcommunity.model




data class Post(
    val id: Long,
    val title: String,
    val content: String,
    val user: User,
    val likes: Int = 0,
    val comments: List<Comment> = emptyList(),
    val commentCount: Int = 0,
    val timestamp: String
)