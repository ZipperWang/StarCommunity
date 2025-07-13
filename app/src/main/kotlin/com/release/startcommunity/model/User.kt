package com.release.startcommunity.model


data class User(
    val id: Long,
    val username: String,
    val email: String,
    val avatar: String = "http://api.starcommunity.asia:54321/uploads/deault.jpg")