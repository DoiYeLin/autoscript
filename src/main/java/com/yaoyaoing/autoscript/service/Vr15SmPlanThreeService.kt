package com.yaoyaoing.autoscript.service

import com.google.gson.Gson
import com.yaoyaoing.autoscript.common.Keys
import com.yaoyaoing.autoscript.common.RedisService
import com.yaoyaoing.autoscript.dao.BetRecordDao
import com.yaoyaoing.autoscript.dto.PlanDto
import com.yaoyaoing.autoscript.dto.Vr15Lottery
import com.yaoyaoing.autoscript.entitys.BetRecord
import com.yaoyaoing.autoscript.entitys.SscLottery
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * 跟投
 */
@Service
class Vr15SmPlanThreeService {

    @Autowired
    lateinit var betRecordDao: BetRecordDao

    @Autowired
    lateinit var redisService: RedisService

    @Autowired
    lateinit var outAccountService: OutAccountService

    data class fxdatas(val unit: Keys.Vr15.Unit, val path: MutableList<Vr15Lottery.Sm.Path>)
    data class fxlp2(val unit: Keys.Vr15.Unit, val count: Int)
    data class redata(val unit: Keys.Vr15.Unit, var type: Vr15Lottery.Sm.Type?)

    companion object {
        const val TAG = "Vr15SmPlanThreeService"
        private val log = LoggerFactory.getLogger(Vr15SmPlanThreeService::class.java)
        private const val bsRedisKey = "rk_vr15_smBs_plan_three"
        private const val dsRedisKey = "rk_vr15_smDs_plan_three"

        private const val stopLoss = "planThree_stopLess"
    }

    fun run(lottery: SscLottery, plan: PlanDto, path: Vr15Lottery.Sm, nextIssue: String) {

    }

    private fun checkLuck(lot: SscLottery, nextIssue: String, plan: PlanDto, unit: Keys.Vr15.Unit, redisKey: String, betTarget: String): Boolean {
        val rdsKey = redisKey + unit.name + betTarget
        val has = redisService.hasKey(rdsKey)
        if (has) {
            val betJson = redisService.hGetAll(rdsKey)
            for (item in betJson.values) {
                val last = Gson().fromJson(Gson().toJson(item), BetRecord::class.java)
                val numres = lotRes(lot.betNumber!!, last.pickMode!!)

                val bean = BetRecord()
                bean.id = last.id
                bean.number = lot.betNumber

                if (numres.contains(last.betTargetPick!!)) {
                    // 中奖
                    bean.status = 1
                    redisService.hDel(rdsKey, last.accId!!)

                    if (plan.runing!!)
                        outAccountService.moneySub(last.accId!!, last.betMoney!! * plan.odds!!)
                    else
                        outAccountService.simMoneyAdd(last.accId!!, last.betMoney!! * plan.odds!!)
                } else {
                    // 未中奖
                    bean.status = 2
                    val nextStep = last.step!!.plus(1)
                    if (nextStep < plan.step!!.size) {
                        val br = BetRecord.vr15Plan611(nextIssue, plan.id, last.accId, last.pickMode!!, last.betTarget!!, last.betTargetPick!!, nextStep, plan.step!!.get(nextStep).money, last.type!!)
                        if (betRecordDao.insertSelective(br) > 0) {
                            redisService.hSet(rdsKey, br.accId!!, br)
                            if (plan.runing!!)
                                outAccountService.moneySub(br.accId!!, br.betMoney!!)
                            else
                                outAccountService.simMoneySub(br.accId!!, br.betMoney!!)
                        }
                    } else {
                        redisService.hDel(rdsKey, last.accId!!)
                    }
                }
                // 修改
                betRecordDao.updateByPrimaryKeySelective(bean)
            }

            return true
        }
        return false
    }

    /**
     * 反向投注值

     */

    /**
     * 开奖号码分析，分析投注的单位正确的押注
     */
    fun lotRes(lotNum: String, unitCode: Int): String {
        val nums = lotNum.map(Character::getNumericValue)
        val num: Int
        var bsSplit = 4
        when (unitCode) {
            Keys.Vr15.Unit.sum.code -> {
                num = nums.sum()
                bsSplit = Keys.Vr15.hebs_divide
            }
            else -> num = nums[unitCode]
        }
        val res = mutableListOf<String>()
        if (num > bsSplit) {
            res.add(Vr15Lottery.Sm.Type.BIG.name)
        } else {
            res.add(Vr15Lottery.Sm.Type.SMALL.name)
        }
        if (num % 2 == 0) {
            res.add(Vr15Lottery.Sm.Type.DOUBLE.name)
        } else {
            res.add(Vr15Lottery.Sm.Type.SINGLE.name)
        }
        return res.joinToString()
    }
}