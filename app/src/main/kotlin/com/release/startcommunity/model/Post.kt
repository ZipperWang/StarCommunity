package com.release.startcommunity.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList


data class Post(
    val id: Long,
    val title: String,
    val content: String,
    val user: User,
    val likes: Int = 0,
    val comments: List<Comment> = emptyList(),
    val timestamp: String
)