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

        fun countGapFromNow(raw: String): List<Long> {
            return try {
                val nowTimeStamp: Long = java.time.LocalDateTime.now()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                val gapSecond: Long = (nowTimeStamp - raw.toLong()) / 1000        //计算每个单位累积时间
                val gapMinute: Long = gapSecond / 60
                val gapHour: Long = gapMinute / 60
                val gapDay: Long = gapHour / 24
                val gapMonth: Long = gapDay / 30
                val gapYear: Long = gapMonth / 12
                listOf(gapSecond, gapMinute, gapHour, gapDay, gapMonth, gapYear)
            } catch (e: Exception){
                return listOf(0, 0, 0, 0, 0, 0)
            }
        }

        fun yieldPostTime(gap: List<Long>): String {
            var result = "刚刚"
            val timeUnitList = listOf("秒", "分钟", "小时", "天")
            if (gap[0] <= 30L){
                return result
            }
            if (gap[3] > 7L){
                return java.time.LocalDate.now().toString()
            }
            for (i in 1..5){
                if (gap[i] == 0L) {
                    result = "${gap[i-1]}${timeUnitList[i-1]}前"
                    break
                }
            }
            return result
        }
    }
}