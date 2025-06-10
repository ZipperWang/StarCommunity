package com.release.startcommunity.model


data class User(
    val id: Long = 0,
    val username: String,
    val password: String,
    val avatar: String = "https://picsum.photos/200/300")