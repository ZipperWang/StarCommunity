package com.release.startcommunity.tool

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Tool {
    companion object {
        fun getCurrentTimestamp(): String {
            return java.time.LocalDateTime.now().toString()
        }
        fun formatTimestamp(raw: String): String {
            return try {
                val millis = raw.toLong()
                val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                sdf.format(Date(millis))
            } catch (e: Exception) {
                raw
            }
        }
    }
}