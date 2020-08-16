package com.yaoyaoing.autoscript.common

interface Time {
    companion object {
        const val MS = 1000
        const val SECOND = 1
        const val MINUTE = SECOND * 60
        const val HALF_HOUR = MINUTE * 30
        const val HOUR = MINUTE * 60
        const val DAY = HOUR * 24
        const val oneDayMillisecond = MS * 3600 * 24 // 一天的毫秒数
    }
}