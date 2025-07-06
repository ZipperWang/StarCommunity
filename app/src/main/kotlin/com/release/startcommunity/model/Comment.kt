package com.release.startcommunity.model



data class Comment (
    val id: Long,
    val comment: String,
    val postId: Long,
    val user: User,
    val timestamp: String
)