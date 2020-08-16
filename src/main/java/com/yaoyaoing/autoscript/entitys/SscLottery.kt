package com.yaoyaoing.autoscript.entitys

import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Table(name = "`ssc_lottery`")
class SscLottery {

    @Id
    @Column(name = "`id`")
    @GeneratedValue(generator = "JDBC")
    var id: String? = null

    @Column(name = "`issue_no`")
    var issueNo: String? = null

    @Column(name = "`bet_number`")
    var betNumber: String? = null

    @Column(name = "`game`")
    var game: String? = null

    @Column(name = "`create_at`")
    var createAt: Long? = null

    companion object {
        const val vrjx_15_day_bet = "vrjx_15_day_bet"
        const val vrjx_15_day_bet_score = "vrjx_15_day_bet_score"


        const val ID = "id"
        const val DB_ID = "id"
        const val ISSUE_NO = "issueNo"
        const val DB_ISSUE_NO = "issue_no"
        const val BET_NUMBER = "betNumber"
        const val DB_BET_NUMBER = "bet_number"
        const val GAME = "game"
        const val DB_GAME = "game"
        const val CREATE_AT = "createAt"
        const val DB_CREATE_AT = "create_at"
        fun defaultInstance(): SscLottery {
            return SscLottery()
        }
    }
}