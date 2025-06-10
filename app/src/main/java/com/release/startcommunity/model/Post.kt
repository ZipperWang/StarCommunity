package com.release.startcommunity.model

import  java.util.UUID

data class Post(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val author: User,
    val likes: Int = 0,
    val comments: List<Comment> = emptyList(),
    val avatar: String = "https://picsum.photos/200/300",
    val images: List<String> = emptyList(),
    val timestamp: String
)            