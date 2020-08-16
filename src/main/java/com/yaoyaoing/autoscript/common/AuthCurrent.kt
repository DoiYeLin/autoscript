package com.yaoyaoing.autoscript.common

object AuthCurrent {
    private val userKey = ThreadLocal<String>()

    fun userKey(): String {
        return userKey.get()
    }

    fun userKey(key: String) {
        userKey.set(key)
    }

    private val platform = ThreadLocal<String>()

    fun platform(): String {
        return platform.get()
    }

    fun platform(key: String) {
        platform.set(key)
    }

    private val supUserId = ThreadLocal<String>()

    fun supUserId(): String {
        return supUserId.get()
    }

    fun supUserId(key: String) {
        supUserId.set(key)
    }

}