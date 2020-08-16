package com.yaoyaoing.autoscript.dto

/**
 * 暂停1期投注，保存上期投注的记录
 */
data class BetPause(
    val issue: String,
    val pickMode: Int,
    /**
     * 方案跟投下标记录，默认0
     */
    var step: Int,
    /**
     * 跟投次数, 同单位投注 N 次不中就换
     */
    var follow: Int,
    /**
     * 连续未中次数
     */
    var noLotLen: Int,

    var account: String,
    var planId: String,
    var ext: String
)