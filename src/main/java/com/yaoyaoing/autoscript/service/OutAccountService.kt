package com.yaoyaoing.autoscript.service

import com.github.pagehelper.page.PageMethod.startPage
import com.google.gson.Gson
import com.yaoyaoing.autoscript.common.AuthCurrent
import com.yaoyaoing.autoscript.common.Keys
import com.yaoyaoing.autoscript.common.RedisService
import com.yaoyaoing.autoscript.common.Ri
import com.yaoyaoing.autoscript.dao.OutAccountDao
import com.yaoyaoing.autoscript.entitys.OutAccount
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tk.mybatis.mapper.entity.Example

@Service
class OutAccountService {

    @Autowired
    private lateinit var redisService: RedisService

    @Autowired
    private lateinit var dao: OutAccountDao

    fun getAccount(): Ri<Any>? {
        println(AuthCurrent.platform())
        println(AuthCurrent.userKey())
        val ex = Example(OutAccount::class.java)
        ex.createCriteria().andEqualTo(OutAccount.PLATFORM, AuthCurrent.platform())
                .andEqualTo(OutAccount.ID, AuthCurrent.userKey())
        val oa: OutAccount? = dao.selectOneByExample(ex)
        if (oa != null) {
            val vo = mapOf(
                    "acc" to oa.account,
                    "pass" to oa.password
            )
            return Ri.successData(vo)
        }
        return Ri.success()
    }


    fun addAccount(account: OutAccount): Ri<OutAccount> {
        val res = dao.insertSelective(account)
        if (res > 0) {
            return Ri.success()
        }
        return Ri.error()
    }

    /**
     * 锁定账号,表示正在使用
     */
    fun grabAcc(type: Int, planId: String): OutAccount? {
        val ex = Example(OutAccount::class.java)
        ex.createCriteria().andEqualTo(OutAccount.TYPE, type).andEqualTo(OutAccount.STATUS, 0)
        startPage<OutAccount>(1, 1)
        val acc = dao.selectOneByExample(ex)
        if (acc != null) {
            val bean = OutAccount()
            bean.id = acc.id
            bean.status = 1
            bean.planId = planId
            val ins = dao.updateByPrimaryKeySelective(bean)
            if (ins > 0) {
                acc.id?.let { redisService.hSet(OutAccount.rk_used_account + planId, it, acc) }
                return acc
            }
        }
        return null
    }

    fun grabAcc(accId: String, planId: String): OutAccount {
        val acc: OutAccount?
        val has = redisService.hHasKey(OutAccount.rk_used_account + planId, accId)
        if (has) {
            val accJson = redisService.hGet(OutAccount.rk_used_account + planId, accId)
            acc = Gson().fromJson(Gson().toJson(accJson), OutAccount::class.java)
        } else {
            acc = dao.selectByPrimaryKey(accId)
            val bean = OutAccount()
            bean.id = acc.id
            bean.status = 1
            bean.planId = planId
            val ins = dao.updateByPrimaryKeySelective(bean)
            if (ins > 0) {
                acc.id?.let { redisService.hSet(OutAccount.rk_used_account + planId, it, acc) }
            }
        }
        return acc
    }

    fun grabAcc(type: Int, planId: String, num: Int): List<OutAccount>? {
        val key = OutAccount.rk_used_account + planId
        if (redisService.hasKey(key)) {
            val accsJson = redisService.hGetAll(key)
            return accsJson.values.map { Gson().fromJson(Gson().toJson(it), OutAccount::class.java) }
        } else {
            val ex = Example(OutAccount::class.java)
            ex.createCriteria().andEqualTo(OutAccount.TYPE, type).andEqualTo(OutAccount.STATUS, 0)
            startPage<OutAccount>(1, num)
            val accs = dao.selectByExample(ex)
            if (accs != null && accs.size > 0) {
                val bean = OutAccount()
                bean.status = 1
                bean.planId = planId

                val ids = accs.map { it.id }
                ex.clear()
                ex.createCriteria().andEqualTo(OutAccount.TYPE, type).andIn(OutAccount.ID, ids)
                val ups = dao.updateByExampleSelective(bean, ex)
                if (ups > 0) {
                    for (acc in accs) {
                        redisService.hSet(key, acc.id!!, acc)
                    }
                    return accs
                }
            }
        }

        return null
    }


    /**
     * 释放方案占用的账号，账号处于空闲状态
     */
    fun deliverAcc(planId: String): Boolean {
        val ex = Example(OutAccount::class.java)
        ex.createCriteria().andEqualTo(OutAccount.PLAN_ID, planId)

        val accs = dao.selectByExample(ex)

        val bean = OutAccount()
        bean.status = 0
        bean.planId = ""
        val ups = dao.updateByExampleSelective(bean, ex)
        if (ups > 0) {
            val accIds = accs.map { it.id }.toTypedArray()
            redisService.hDel(OutAccount.rk_used_account + planId, accIds)
            return true
        }
        return false
    }

    /**
     * 系统启动时释放所有账号
     */
    fun deliverAllAcc() {
        val ex = Example(OutAccount::class.java)
        ex.createCriteria().andEqualTo(OutAccount.STATUS, 1)
        val bean = OutAccount()
        bean.status = 0
        bean.planId = ""
        val ups = dao.updateByExampleSelective(bean, ex)
        if (ups > 0) {
            val plans = listOf(Keys.Vr15.rk_sm_plans)
            for (plan in plans) {
                val pm = redisService.hGetAll(plan)
                for (pid in pm) {
                    redisService.del(OutAccount.rk_used_account + pid)
                }
            }
        }
    }

    fun moneySub(accId: String, money: Double): Int {
        return dao.moneySub(accId, money)
    }

    fun moneyAdd(accId: String, money: Double): Int {
        return dao.moneyAdd(accId, money)
    }

    fun simMoneySub(accId: String, money: Double): Int {
        return dao.simMoneySub(accId, money)
    }

    fun simMoneyAdd(accId: String, money: Double): Int {
        return dao.simMoneyAdd(accId, money)
    }


}