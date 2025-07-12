package com.release.startcommunity.tool

abstract class FunctionalWidget {
    abstract val name: String
    abstract val info: String
    fun tellAbout() {
        println("Widget $name: $info")
    }
}