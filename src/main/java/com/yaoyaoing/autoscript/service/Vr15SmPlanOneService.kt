package com.yaoyaoing.autoscript.service

import com.google.gson.Gson
import com.yaoyaoing.autoscript.common.Keys
import com.yaoyaoing.autoscript.common.RedisService
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
class Vr15SmPlanOneService {

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
        const val TAG = "Vr15SmPlanOneService"
        private val log = LoggerFactory.getLogger(Vr15SmPlanOneService::class.java)
        private const val bsRedisKey = "rk_vr15_smBs_plan_one"
        private const val dsRedisKey = "rk_vr15_smDs_plan_one"

        private const val stopLoss = "planOne_stopLess"
        private const val start = 5
    }

    fun run(lottery: SscLottery, plan: PlanDto, path: Vr15Lottery.Sm, unit: Keys.Vr15.Unit, nextIssue: String) {
        val bsPaths = mutableListOf(fxdatas(Keys.Vr15.Unit.sum, path.hbset), fxdatas(Keys.Vr15.Unit.wan, path.wbset), fxdatas(Keys.Vr15.Unit.qian, path.qbset), fxdatas(Keys.Vr15.Unit.bai, path.bbset), fxdatas(Keys.Vr15.Unit.shi, path.sbset), fxdatas(Keys.Vr15.Unit.ge, path.gbset))
        val dsPaths = mutableListOf(fxdatas(Keys.Vr15.Unit.sum, path.hdset), fxdatas(Keys.Vr15.Unit.wan, path.wdset), fxdatas(Keys.Vr15.Unit.qian, path.qdset), fxdatas(Keys.Vr15.Unit.bai, path.bdset), fxdatas(Keys.Vr15.Unit.shi, path.sdset), fxdatas(Keys.Vr15.Unit.ge, path.gdset))
        lateinit var bset: MutableList<Vr15Lottery.Sm.Path>
        lateinit var dset: MutableList<Vr15Lottery.Sm.Path>
        when (unit) {
            Keys.Vr15.Unit.sum -> {
                bset = path.hbset
                dset = path.hdset
            }
            Keys.Vr15.Unit.wan -> {
                bset = path.wbset
                dset = path.wdset
            }
            Keys.Vr15.Unit.qian -> {
                bset = path.qbset
                dset = path.qdset
            }
            Keys.Vr15.Unit.bai -> {
                bset = path.bbset
                dset = path.bdset
            }
            Keys.Vr15.Unit.shi -> {
                bset = path.sbset
                dset = path.sdset
            }
            else -> {
                bset = path.gbset
                dset = path.gdset
            }
        }
        bsPaths.removeIf { it.unit == unit }
        dsPaths.removeIf { it.unit == unit }

        println(lottery.issueNo)

        val bscl = checkLuck(lottery, nextIssue, plan, unit, bsRedisKey, Keys.Vr15.BS)
        if (!bscl) {
            val bsbet = this.fenxi(bsPaths, bset, unit)
            beginBet(nextIssue, plan, unit, bsbet, Keys.Vr15.BS, bsRedisKey)
        }

        val dscl = checkLuck(lottery, nextIssue, plan, unit, dsRedisKey, Keys.Vr15.DS)
        if (!dscl) {
            val dsbet = this.fenxi(dsPaths, dset, unit)
            beginBet(nextIssue, plan, unit, dsbet, Keys.Vr15.DS, dsRedisKey)
        }
    }

    /**
     * 检查已投注是否中奖
     */
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
     * 开始投注
     */
    private fun beginBet(nextIssue: String, plan: PlanDto, unit: Keys.Vr15.Unit, betArg: redata?, betTarget: String, redisKey: String) {
        if (betArg != null) {
            val rdsKey = redisKey + unit.name + betTarget
            val betType = if (plan.runing!!) OutAccount.Type.runing.code else OutAccount.Type.simulate.code
            var accs: List<OutAccount>? = null
            if (plan.runing!!) {
                accs = outAccountService.grabAcc(OutAccount.Type.runing.code, plan.id, plan.accNum)
            } else if (plan.simulate!!) {
                accs = outAccountService.grabAcc(OutAccount.Type.simulate.code, plan.id, plan.accNum)
            }
            accs?.run {
                for (item in this) {
                    println("$nextIssue - ${betArg.unit.name} - ${betArg.unit.code}")
                    val br = BetRecord.vr15Plan611(nextIssue, plan.id, item.id!!, betArg.unit.code, betTarget, betArg.type!!.name, 0, plan.step?.get(0)!!.money, betType)
                    if (betRecordDao.insertSelective(br) > 0) {
                        redisService.hSet(rdsKey, br.accId!!, br)
                        if (plan.runing!!)
                            outAccountService.moneySub(br.accId!!, br.betMoney!!)
                        else
                            outAccountService.simMoneySub(br.accId!!, br.betMoney!!)
                    }
                }
            }
        }
    }

    /**
     * 分析
     */
    private fun fenxi(paths: List<fxdatas>, path: MutableList<Vr15Lottery.Sm.Path>, unit: Keys.Vr15.Unit): redata? {
        if (path[0].count > start) {
            val type = path[0].type
            // 相同投注值连出>3跟投
            val alikeUnit = mutableListOf<redata>()
            // 反向投注值连出>3跟头
            val varyUnit = mutableListOf<redata>()
            // 5期开奖路单
            var lp2 = mutableListOf<fxlp2>()
            for (data in paths) {
//                for (j in 3 downTo 2) {
//                    if (data.path.size >= j) {
//                        val count = data.path.subList(0, j).sumBy { it.count }
//                        lp2.add(fxlp2(data.unit, count))
//                        break
//                    }
//                }
                if (data.path.size >= 2) {
                    val count = data.path.subList(0, 2).sumBy { it.count }
                    lp2.add(fxlp2(data.unit, count))
                }

                val temp = data.path[0]
                if (temp.count > 4) {
                    if (temp.type == type) {
                        alikeUnit.add(redata(data.unit, temp.type))
                    } else {
                        varyUnit.add(redata(data.unit, temp.type))
                    }
                }
            }

//            println("alike - $alikeUnit")
//            println("vary - $varyUnit")s

            val resData: redata
//            var lastType: Vr15Lottery.Sm.Type = type
            val betTargetVal: Vr15Lottery.Sm.Type
            if (alikeUnit.size > 0) {
                resData = alikeUnit[0]
            } else if (varyUnit.size > 0) {
                resData = varyUnit[0]
            } else {
                lp2 = lp2.sortedByDescending { it.count }.toMutableList()
                println("lp2 - $lp2")
                betTargetVal = when (type) {
                    Vr15Lottery.Sm.Type.BIG -> Vr15Lottery.Sm.Type.SMALL
                    Vr15Lottery.Sm.Type.SMALL -> Vr15Lottery.Sm.Type.BIG
                    Vr15Lottery.Sm.Type.DOUBLE -> Vr15Lottery.Sm.Type.SINGLE
                    else -> Vr15Lottery.Sm.Type.DOUBLE
                }
                resData = redata(lp2[0].unit, betTargetVal)
            }
            return resData
        }
        return null
    }

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