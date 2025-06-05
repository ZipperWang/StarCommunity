package com.release.startcommunity.model

import  java.util.UUID

data class Post(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val author: String,
    val timestamp: String
)