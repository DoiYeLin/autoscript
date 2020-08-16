package com.yaoyaoing.autoscript.entitys

import lombok.EqualsAndHashCode
import lombok.Getter
import lombok.Setter
import lombok.ToString
import lombok.experimental.Accessors
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Table(name = "`out_account`")
class OutAccount {

    @Id
    @Column(name = "`id`")
    @GeneratedValue(generator = "JDBC")
    var id: String? = null

    @Column(name = "`account`")
    var account: String? = null

    @Column(name = "`password`")
    var password: String? = null

    @Column(name = "`platform`")
    var platform: String? = null

    /**
     * 账号金额
     */
    @Column(name = "`money`")
    var money: Double? = null

    /**
     * 模拟金额
     */
    @Column(name = "`sim_money`")
    var simMoney: Double? = null

    /**
     * 账号类型
     * 0：模拟投注账号
     * 1：真实投注账号
     */
    @Column(name = "`type`")
    var type: Int? = null

    /**
     * 状态
     * 0：正常
     * 1：占用
     */
    @Column(name = "`status`")
    var status: Int? = null

    @Column(name = "`plan_id`")
    var planId: String? = null

    companion object {
        const val rk_used_account = "rk_used_account"

        const val ID = "id"
        const val DB_ID = "id"
        const val ACCOUNT = "account"
        const val DB_ACCOUNT = "account"
        const val PASSWORD = "password"
        const val DB_PASSWORD = "password"
        const val PLATFORM = "platform"
        const val DB_PLATFORM = "platform"
        const val MONEY = "money"
        const val DB_MONEY = "money"
        const val TYPE = "type"
        const val DB_TYPE = "type"
        const val STATUS = "status"
        const val DB_STATUS = "status"
        const val PLAN_ID = "planId"
        const val DB_PLAN_ID = "planId"
        fun defaultInstance(): OutAccount {
            return OutAccount()
        }
    }

    enum class Type(val code: Int) {
        simulate(0),
        runing(1)
    }
}