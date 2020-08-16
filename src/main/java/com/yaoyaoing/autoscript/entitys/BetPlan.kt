package com.yaoyaoing.autoscript.entitys

import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Table(name = "`bet_plan`")
class BetPlan {
    @Id
    @Column(name = "`id`")
    @GeneratedValue(generator = "JDBC")
    var id: String? = null

    @Column(name = "`game`")
    var game: String? = null

    @Column(name = "`play_mode`")
    var playMode: String? = null

    @Column(name = "`key`")
    var key: Int? = null

    @Column(name = "`name`")
    var name: String? = null

    @Column(name = "`value`")
    var value: String? = null

    /**
     * 是否正在模拟
     */
    @Column(name = "`simulate`")
    var simulate: Boolean? = null

    /**
     * 是否正在运行
     */
    @Column(name = "`runing`")
    var runing: Boolean? = null

    @Column(name = "`create_at`")
    var createAt: Long? = 0

    /**
     * 选中下注的单位
     */
    @Column(name = "`pick_mode`")
    var pickMode: String? = null

    /**
     * 赔率
     */
    @Column(name = "`odds`")
    var odds: Double? = null

    /**
     * 运行账号数量
     */
    @Column(name = "`acc_num`")
    var accNum: Int? = null

    companion object {
        const val ID = "id"
        const val DB_ID = "id"
        const val GAME = "game"
        const val DB_GAME = "game"
        const val PLAY_MODE = "playMode"
        const val DB_PLAY_MODE = "play_mode"
        const val KEY = "key"
        const val DB_KEY = "key"
        const val NAME = "name"
        const val DB_NAME = "name"
        const val VALUE = "value"
        const val DB_VALUE = "value"
        const val SIMULATE = "simulate"
        const val DB_SIMULATE = "simulate"
        const val RUNING = "runing"
        const val DB_RUNING = "runing"
        const val CREATE_AT = "createAt"
        const val DB_CREATE_AT = "create_at"
        fun defaultInstance(): BetPlan {
            return BetPlan()
        }
    }
}