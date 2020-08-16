package com.yaoyaoing.autoscript.entitys

import cn.hutool.core.util.IdUtil
import com.yaoyaoing.autoscript.common.Keys
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Transient

@Table(name = "`bet_record`")
class BetRecord {
    @Id
    @Column(name = "`id`")
    @GeneratedValue(generator = "JDBC")
    var id: String? = null

    /**
     * 投注账号
     */
    @Column(name = "`acc_id`")
    var accId: String? = null

    /**
     * 方案ID
     */
    @Column(name = "plan_id")
    var planId: String? = null

    /**
     * 期号
     */
    @Column(name = "`issue_no`")
    var issueNo: String? = null

    @Column(name = "`game`")
    var game: String? = null

    /**
     * 玩法
     */
    @Column(name = "`play_mode`")
    var playMode: String? = null

    /**
     * 下注单位
     * 和：-1
     * 万：0
     * 千：1
     * 百：2
     * 十：3
     * 个：4
     */
    @Column(name = "`pick_mode`")
    var pickMode: Int? = null

    /**
     * 投注目标
     * bs：大小
     * ds：单双
     */
    @Column(name = "`bet_target`")
    var betTarget: String? = null

    /**
     * 投注起始目标
     */
    @Column(name = "`bet_target_start`")
    var betTargetStart: String? = null

    /**
     * 投注目标值
     * BIG 大,
     * SMALL 小,
     * DOUBLE 双,
     * SINGLE 单
     */
    @Column(name = "`bet_target_pick`")
    var betTargetPick: String? = null

    /**
     * 投注金额
     */
    @Column(name = "`bet_money`")
    var betMoney: Double? = null

    /**
     * 状态
     * 0：未开奖
     * 1：中奖
     * 2：未中
     */
    @Column(name = "`status`")
    var status: Int? = null

    /**
     * 投注类型:
     * 0：模拟
     * 1：真实下注
     */
    @Column(name = "`type`")
    var type: Int? = null

    @Column(name = "`desc`")
    var desc: String? = null

    @Column(name = "`create_at`")
    var createAt: Long? = null

    /**
     * 开奖号码
     */
    @Column(name = "`number`")
    var number: String? = null

    /**
     * 方案跟投下标记录，默认0
     */
    @Column(name = "`step`")
    var step: Int? = null

    /**
     * 跟投次数, 同单位投注 N 次不中就换
     */
    @Column(name = "`follow`")
    var follow: Int? = null

    /**
     * 连续未中次数
     */
    @Column(name = "`no_lot_len`")
    var noLotLen: Int? = null

    companion object {
        const val ID = "id"
        const val DB_ID = "id"
        const val ACC_ID = "accId"
        const val DB_ACC_ID = "acc_id"
        const val ISSUE_NO = "issueNo"
        const val DB_ISSUE_NO = "issue_no"
        const val GAME = "game"
        const val DB_GAME = "game"
        const val PLAY_MODE = "playMode"
        const val DB_PLAY_MODE = "play_mode"
        const val PICK_MODE = "pickMode"
        const val DB_PICK_MODE = "pick_mode"
        const val BET_TARGET = "betTarget"
        const val DB_BET_TARGET = "bet_target"

        const val BET_TARGET_PICK = "betTargetPick"
        const val DB_BET_TARGET_PICK = "bet_target_pick"


        const val BET_MONEY = "betMoney"
        const val DB_BET_MONEY = "bet_money"
        const val STATUS = "status"
        const val DB_STATUS = "status"
        const val TYPE = "type"
        const val DB_TYPE = "type"
        const val DESC = "desc"
        const val DB_DESC = "desc"
        const val CREATE_AT = "createAt"
        const val DB_CREATE_AT = "create_at"
        fun defaultInstance(): BetRecord {
            return BetRecord()
        }

        /**
         * 反投方案
         * 第6局开始
         */
        fun vr15Plan611(issue: String, planId: String, accId: String? = null, pickMode: Int, betTarget: String, betTargetPick: String, planStep: Int, betMoney: Double, type: Int): BetRecord {
            val br = BetRecord()
            br.id = IdUtil.objectId()
            br.game = Keys.Vr15.key
            br.planId = planId
            br.accId = accId
            br.issueNo = issue
            br.playMode = Keys.Vr15.sm
            br.pickMode = pickMode
            br.betTarget = betTarget
            br.betTargetPick = betTargetPick
            br.step = planStep
            br.betMoney = betMoney
            br.status = 0
            br.type = type
            return br
        }
    }
}