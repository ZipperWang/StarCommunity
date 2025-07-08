package com.release.startcommunity.model
import android.util.Log


open class Event<out T>(private val content: T) {        //定义事件类
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {        //确保事件只触发1次，多次触发返回null
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            eventLog()
            content
        }
    }

    private fun eventLog() {
        Log.i("Toast Information", "预Toast内容${content?.toString()}")
    }
}