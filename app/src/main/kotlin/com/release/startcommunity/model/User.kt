package com.release.startcommunity.model


data class User(
    val username: String,
    val password: String,
    val email: String,
    val avatar: String = "http://47.121.204.76:8080/uploads/deault.jpg")