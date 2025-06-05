package com.release.startcommunity.tool

class Tool {
    companion object {
        fun getCurrentTimestamp(): String {
            return java.time.LocalDateTime.now().toString()
        }
    }
}