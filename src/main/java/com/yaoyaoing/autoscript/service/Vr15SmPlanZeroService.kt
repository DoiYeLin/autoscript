package com.yaoyaoing.autoscript.service

import com.google.gson.Gson
import com.yaoyaoing.autoscript.common.Keys
import com.yaoyaoing.autoscript.common.RedisService
import com.yaoyaoing.autoscript.common.Ri
import com.yaoyaoing.autoscript.dao.BetRecordDao
import com.yaoyaoing.autoscript.dto.PlanDto
import com.yaoyaoing.autoscript.dto.Vr15Lottery
import com.yaoyaoing.autoscript.entitys.BetRecord
import com.yaoyaoing.autoscript.entitys.OutAccount
import com.yaoyaoing.autoscript.entitys.SscLottery
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
class Vr15SmPlanZeroService {

    companion object {
        private val log = LoggerFactory.getLogger(Vr15SmPlanZeroService::class.java)
        val rkbs = "rk_vr15_smBs_plan_zero"
        val rkds = "rk_vr15_smDs_plan_zero"

        /**
         * 止损记录Key
         * {sum: 0, wan: 0, qian: 0, bai: 0, shi: 0, ge: 0}
         */
        val rkStopLoss = "sm_plan_zero_stoploss"
        val start = 6
    }

    @Autowired
    lateinit var betRecordDao: BetRecordDao

    @Autowired
    lateinit var redisService: RedisService

    @Autowired
    lateinit var outAccountService: OutAccountService


    /**
     * 大小投注
     * @param lottery 开奖号码
     * @param plan 投注方案
     * @param path 最后一期开奖号码路单
     * @param unit 投注目标单位, 和/万...
     */
    private fun run(rk: String, lottery: SscLottery, plan: PlanDto, path: Vr15Lottery.Sm.Path, unit: Keys.Vr15.Unit, nextIssue: String) {

        // 投注缓存Key 和 投注单位的选中值
        lateinit var betTargetPick: Vr15Lottery.Sm.Type
        lateinit var betTarget: String

        when (path.type) {
            Vr15Lottery.Sm.Type.BIG -> {
                betTargetPick = Vr15Lottery.Sm.Type.SMALL
                betTarget = Keys.Vr15.BS
            }
            Vr15Lottery.Sm.Type.SMALL -> {
                betTargetPick = Vr15Lottery.Sm.Type.BIG
                betTarget = Keys.Vr15.BS
            }
            Vr15Lottery.Sm.Type.DOUBLE -> {
                betTargetPick = Vr15Lottery.Sm.Type.SINGLE
                betTarget = Keys.Vr15.DS
            }
            else -> {
                betTarget = Keys.Vr15.DS
                betTargetPick = Vr15Lottery.Sm.Type.DOUBLE
            }
        }

        // 检查是否投注，有没有中奖
        val ri = this.luck(lottery, rk, plan, nextIssue)

        if (path.count > start) {

            // 投注类型，真实还是模拟
            val betType = if (plan.runing!!) OutAccount.Type.runing.code else OutAccount.Type.simulate.code

            // 上期投注过
            if (ri.code != 200) {
                var accs: List<OutAccount>? = null
                if (plan.runing!!) {
                    accs = outAccountService.grabAcc(OutAccount.Type.runing.code, plan.id, plan.accNum)
                } else if (plan.simulate!!) {
                    accs = outAccountService.grabAcc(OutAccount.Type.simulate.code, plan.id, plan.accNum)
                }
                if (accs != null) {
                    accs.forEach(Consumer {
                        val br = BetRecord.vr15Plan611(nextIssue, plan.id, it.id!!, unit.code, betTarget, betTargetPick.name, 0, plan.step?.get(0)!!.money, betType)
                        if (betRecordDao.insertSelective(br) > 0) {
                            redisService.hSet(rk, br.accId!!, br)
                            if (plan.runing!!)
                                outAccountService.moneySub(br.accId!!, br.betMoney!!)
                            else
                                outAccountService.simMoneySub(br.accId!!, br.betMoney!!)
                        }
                    })
                }
            }
        }
    }

    fun runbs(lottery: SscLottery, plan: PlanDto, path: Vr15Lottery.Sm.Path, unit: Keys.Vr15.Unit, nextIssue: String) {
        val rk = rkbs + plan.id + unit.name
        this.run(rk, lottery, plan, path, unit, nextIssue)
    }

    /**
     * 单双投注
     * @param lottery 开奖号码
     * @param plan 投注方案
     * @param path 最后一期开奖号码路单
     * @param unit 投注目标单位, 和/万...
     */
    fun runds(lottery: SscLottery, plan: PlanDto, path: Vr15Lottery.Sm.Path, unit: Keys.Vr15.Unit, nextIssue: String) {
        val rk = rkds + plan.id + unit.name
        this.run(rk, lottery, plan, path, unit, nextIssue)
    }

    /**
     * 验证是否中奖，没有中奖倍投
     */
    fun luck(lottery: SscLottery, rk: String, plan: PlanDto, nextIssue: String): Ri<BetRecord> {
        // 本期号码是否下注
        if (redisService.hasKey(rk)) {
            val json = redisService.hGetAll(rk)
            val vals = json.values
            // 一期号码多个账号下注
            for (item in vals) {
                // 投注缓存
                val last = Gson().fromJson(Gson().toJson(item), BetRecord::class.java)
                // 分析投注号码押注
                val numres = lotNumAnalysis(lottery.betNumber!!, last.pickMode!!)

                var bean = BetRecord()
                bean.id = last.id
                bean.number = lottery.betNumber
                // 中奖了
                if (numres.contains(last.betTargetPick!!)) {
                    bean.status = 1
                    redisService.hDel(rk, last.accId!!)

                    if (plan.runing!!)
                        outAccountService.moneySub(last.accId!!, last.betMoney!! * plan.odds!!)
                    else
                        outAccountService.simMoneyAdd(last.accId!!, last.betMoney!! * plan.odds!!)
                } else {
                    bean.status = 2
                    // 没有中奖，重新投注
                    val nextStep = last.step!!.plus(1)
                    if (nextStep < plan.step!!.size) {
                        var betTargetPick: String
                        betTargetPick = last.betTargetPick!!
                        val br = BetRecord.vr15Plan611(nextIssue, plan.id, last.accId, last.pickMode!!, last.betTarget!!, betTargetPick, nextStep, plan.step!!.get(nextStep).money, last.type!!)
                        if (betRecordDao.insertSelective(br) > 0) {
                            redisService.hSet(rk, br.accId!!, br)
                            if (plan.runing!!)
                                outAccountService.moneySub(br.accId!!, br.betMoney!!)
                            else
                                outAccountService.simMoneySub(br.accId!!, br.betMoney!!)
                        }
                    } else {
                        redisService.hDel(rk, last.accId!!)
                    }
                }
                betRecordDao.updateByPrimaryKeySelective(bean)
            }
            return Ri.success()
        }
        return Ri.error()
    }


    /**
     * 开奖号码分析，分析投注的单位正确的押注
     */
    fun lotNumAnalysis(lotNum: String, unitCode: Int): String {
        val nums = lotNum.map(Character::getNumericValue)
        var num: Int
        var bsSplit = 4
        when (unitCode) {
            Keys.Vr15.Unit.sum.code -> {
                num = nums.sum()
                bsSplit = Keys.Vr15.hebs_divide
            }
            else -> num = nums[unitCode]
        }
        var res = mutableListOf<String>()
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