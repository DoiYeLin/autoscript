package com.yaoyaoing.autoscript

import cn.hutool.core.date.DateUtil
import cn.hutool.core.util.IdUtil
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr
import com.google.gson.Gson
import com.yaoyaoing.autoscript.common.Keys
import com.yaoyaoing.autoscript.common.RedisService
import com.yaoyaoing.autoscript.dao.SscLotteryDao
import com.yaoyaoing.autoscript.dto.Plan
import com.yaoyaoing.autoscript.dto.PlanDto
import com.yaoyaoing.autoscript.dto.Vr15Lottery
import com.yaoyaoing.autoscript.entitys.BetRecord
import com.yaoyaoing.autoscript.entitys.OutAccount
import com.yaoyaoing.autoscript.entitys.SscLottery
import com.yaoyaoing.autoscript.service.Vr15SmPlanService
import com.yaoyaoing.autoscript.utils.MyUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import tk.mybatis.mapper.entity.Example
import java.util.*
import java.util.function.Consumer
import javax.xml.stream.events.Characters
import kotlin.streams.toList

@ExperimentalStdlibApi
@RunWith(SpringRunner::class)
@SpringBootTest(classes = [AutoScriptApplication::class])
@Transactional
open class PlanTest {
    @Autowired
    private lateinit var redisService: RedisService

    @Autowired
    lateinit var lotteryDao: SscLotteryDao

    @Autowired
    lateinit var vr15SmPlanService: Vr15SmPlanService

    @Test
    fun read() {
        val json = redisService.hGet(Keys.Vr15.rk_sm_plans, "5f10bdc12dced657ed11ddea")
//        val plan = JSON.parseObject(json.toString(), Vr15SmDto::class.java)
        val plan = Gson().fromJson(json.toString(), PlanDto::class.java)
        println(plan)
    }


    @Test
    fun planSim() {
        val sls = getSls("", "")

        val planJson =
                "{\"step\":[{\"failJump\":2,\"failJumpPlan\":null,\"money\":1.0,\"winJump\":1},{\"failJump\":3,\"failJumpPlan\":null,\"money\":2.0,\"winJump\":1},{\"failJump\":4,\"failJumpPlan\":null,\"money\":4.0,\"winJump\":1},{\"failJump\":5,\"failJumpPlan\":null,\"money\":9.0,\"winJump\":1},{\"failJump\":1,\"failJumpPlan\":null,\"money\":19.0,\"winJump\":1}],\"id\":\"5f133b01a003664a4207056a\",\"game\":\"vr15\",\"playMode\":\"vr15sm\",\"key\":2,\"name\":\"大小单双\",\"simulate\":true,\"runing\":false,\"pickMode\":\"h_bs,h_ds\",\"odds\":1.96}"
        val smp = Gson().fromJson(planJson, PlanDto::class.java)

        var money: Double = 1000.000

        var step: Int = 0
        var cutLoss = 0
        // 大小
        var bs = -1

        var curInx = -1
        for ((inx, item) in sls!!.withIndex()) {
            var res: Int = -1
            val planStep = smp.step!!.get(step)
            val bns = item.betNumber!!.map(Character::getNumericValue).toList()
            if (bs > -1) {
                if (curInx == 0) {
                    res = bns.sum()
                    if (res > 22) {
                        money = money + planStep.money * smp.odds!!
                        println("${inx} - 号码：${res} - 投注：${planStep.money} 剩余： ${money}  ===========  ${res} ===========")
                        step = 0
                    } else {
                        println("${inx} - 号码：${res} - 投注：${planStep.money} 剩余： ${money}  -----------  ${res}")
                        step++
                    }
                } else {
                    res = bns.get(curInx - 1)
                    if (res > 4) {
                        money = money + planStep.money * smp.odds!!
                        println("${inx} - 号码：${res} - 投注：${planStep.money} 剩余： ${money}  ===========  ${res} ===========")
                        step = 0
                    } else {
                        println("${inx} - 号码：${res} - 投注：${planStep.money} 剩余： ${money}  -----------  ${res}")
                        step++
                    }
                }
            }



            if (bs < 0) {
                bs = 0
            }
            curInx++
            if (curInx == 5) {
                curInx = 0
            }
            money = money - planStep.money

            if (step == 5) {
                step = 0
                cutLoss++
                println("${inx}  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  ${cutLoss}")
            }
        }
        println("############## ${money}")
    }

    fun getSls(start: String, end: String): MutableList<SscLottery>? {
        val curday = DateUtil.parse(MyUtils.currentTime(MyUtils.date_time_day_str)).time / 1000
        val no = MyUtils.currentTime("yyyyMMdd")
        var ex = Example(SscLottery::class.java)
        ex.createCriteria().andGreaterThanOrEqualTo(SscLottery.ISSUE_NO, start)
                .andLessThan(SscLottery.ISSUE_NO, end)
        val sls = lotteryDao.selectByExample(ex)
        return sls
    }

    @Test
    @Rollback(false)
    fun planSimTest() {
        val plans = redisService.hGet(Keys.Vr15.rk_sm_plans, "5f133b01a003664a4207056a")
        val smp = Gson().fromJson(Gson().toJson(plans), PlanDto::class.java)
        val sls = getSls("20200805", "20200806")
        for (item in sls!!) {
            val waitOpenIssue = (item.issueNo!!.toLong() + 1).toString()
            vr15SmPlanService.plan2(item, null, waitOpenIssue, smp, null)
        }
    }

    @ExperimentalStdlibApi
    @Test
    @Rollback(false)
    fun planTest() {
//        val plans = redisService.hGet(Keys.Vr15.rk_sm_plans, "5f143476a00331d1e6dd79e7")
//        val smp = Gson().fromJson(Gson().toJson(plans), PlanDto::class.java)
        val issues = listOf(
                listOf("20200726", "20200727")
        )
        for (issue in issues) {
            for ((inx, iss) in issue.withIndex()) {
                if (inx > issue.size - 2) {
                    break
                }
                val sls = getSls(iss, issue.get(inx + 1))
                if (sls == null || sls.size == 0) {
                    continue
                }
                var numbers = mutableListOf<List<Int>>()
                sls.forEach {
                    if (numbers.size == 50) {
                        numbers.removeLast()
                    }
                    val nums = it.betNumber!!.map(Character::getNumericValue)
                    numbers.add(0, nums)
                    vr15SmPlanService.lottery(it, numbers)
                }
            }
        }

        /**
         * 获取开奖结果，只计算第一次入库的结果
         */
    }

    @Test
    fun sscLotterSustainAlike() {
        val issues = listOf(
                listOf("20200721", "20200722", "20200723", "20200724", "20200725", "20200726", "20200727"),
                listOf("20200728", "20200729"),
                listOf("20200801", "20200802", "20200803", "20200804", "20200805", "20200806", "20200807", "20200808", "20200809", "20200810")
        )
        val counts = mutableListOf<Int>()
        for (issue in issues) {
            for ((inx, iss) in issue.withIndex()) {
                if (inx > issue.size - 2) {
                    break
                }

                val sls = getSls(iss, issue.get(inx + 1))
                if (sls == null || sls.size == 0) {
                    continue
                }


                val nums = sls.map { it.betNumber!!.map(Character::getNumericValue) }


                val res = vr15SmPlanService.analysisPath(nums)
                println()
                println()
                println()
                println(iss)
                var br: Int
                var be: Int
                var sr: Int
                var se: Int


                val hbset = res.hbset.stream().sorted(compareByDescending { it.count }).toList().groupBy { it.type }.toMap()
                br = hbset.get(Vr15Lottery.Sm.Type.BIG)!!.filter { it.count > 6 && it.count < 12 }.count()
                be = hbset.get(Vr15Lottery.Sm.Type.BIG)!!.filter { it.count >= 12 }.count()
                sr = hbset.get(Vr15Lottery.Sm.Type.SMALL)!!.filter { it.count > 6 && it.count < 12 }.count()
                se = hbset.get(Vr15Lottery.Sm.Type.SMALL)!!.filter { it.count >= 12 }.count()
                counts.addAll(res.hbset.map { it.count })
                println("和 -> 大： ${br} > ${be} -  小： ${sr} > ${se}")
                val hdset = res.hdset.stream().sorted(compareByDescending { it.count }).toList().groupBy { it.type }.toMap()
                br = hdset.get(Vr15Lottery.Sm.Type.DOUBLE)!!.filter { it.count > 6 && it.count < 12 }.count()
                be = hdset.get(Vr15Lottery.Sm.Type.DOUBLE)!!.filter { it.count >= 12 }.count()
                sr = hdset.get(Vr15Lottery.Sm.Type.SINGLE)!!.filter { it.count > 6 && it.count < 12 }.count()
                se = hdset.get(Vr15Lottery.Sm.Type.SINGLE)!!.filter { it.count >= 12 }.count()
                counts.addAll(res.hdset.map { it.count })
                println("和 -> 双： ${br} > ${be} -  单： ${sr} > ${se}")

                val wbset = res.wbset.stream().sorted(compareByDescending { it.count }).toList().groupBy { it.type }.toMap()
                br = wbset.get(Vr15Lottery.Sm.Type.BIG)!!.filter { it.count > 6 && it.count < 12 }.count()
                be = wbset.get(Vr15Lottery.Sm.Type.BIG)!!.filter { it.count >= 12 }.count()
                sr = wbset.get(Vr15Lottery.Sm.Type.SMALL)!!.filter { it.count > 6 && it.count < 12 }.count()
                se = wbset.get(Vr15Lottery.Sm.Type.SMALL)!!.filter { it.count >= 12 }.count()
                counts.addAll(res.wbset.map { it.count })
                println("万 -> 大： ${br} > ${be} -  小： ${sr} > ${se}")
                val wdset = res.wdset.stream().sorted(compareByDescending { it.count }).toList().groupBy { it.type }.toMap()
                br = wdset.get(Vr15Lottery.Sm.Type.DOUBLE)!!.filter { it.count > 6 && it.count < 12 }.count()
                be = wdset.get(Vr15Lottery.Sm.Type.DOUBLE)!!.filter { it.count >= 12 }.count()
                sr = wdset.get(Vr15Lottery.Sm.Type.SINGLE)!!.filter { it.count > 6 && it.count < 12 }.count()
                se = wdset.get(Vr15Lottery.Sm.Type.SINGLE)!!.filter { it.count >= 12 }.count()
                counts.addAll(res.wdset.map { it.count })
                println("万 -> 双： ${br} > ${be} -  单： ${sr} > ${se}")

                val qbset = res.qbset.stream().sorted(compareByDescending { it.count }).toList().groupBy { it.type }.toMap()
                br = qbset.get(Vr15Lottery.Sm.Type.BIG)!!.filter { it.count > 6 && it.count < 12 }.count()
                be = qbset.get(Vr15Lottery.Sm.Type.BIG)!!.filter { it.count >= 12 }.count()
                sr = qbset.get(Vr15Lottery.Sm.Type.SMALL)!!.filter { it.count > 6 && it.count < 12 }.count()
                se = qbset.get(Vr15Lottery.Sm.Type.SMALL)!!.filter { it.count >= 12 }.count()
                counts.addAll(res.qbset.map { it.count })
                println("千 -> 大： ${br} > ${be} -  小： ${sr} > ${se}")
                val qdset = res.qdset.stream().sorted(compareByDescending { it.count }).toList().groupBy { it.type }.toMap()
                br = qdset.get(Vr15Lottery.Sm.Type.DOUBLE)!!.filter { it.count > 6 && it.count < 12 }.count()
                be = qdset.get(Vr15Lottery.Sm.Type.DOUBLE)!!.filter { it.count >= 12 }.count()
                sr = qdset.get(Vr15Lottery.Sm.Type.SINGLE)!!.filter { it.count > 6 && it.count < 12 }.count()
                se = qdset.get(Vr15Lottery.Sm.Type.SINGLE)!!.filter { it.count >= 12 }.count()
                counts.addAll(res.qdset.map { it.count })
                println("千 -> 双： ${br} > ${be} -  单： ${sr} > ${se}")

                val bbset = res.bbset.stream().sorted(compareByDescending { it.count }).toList().groupBy { it.type }.toMap()
                br = bbset.get(Vr15Lottery.Sm.Type.BIG)!!.filter { it.count > 6 && it.count < 12 }.count()
                be = bbset.get(Vr15Lottery.Sm.Type.BIG)!!.filter { it.count >= 12 }.count()
                sr = bbset.get(Vr15Lottery.Sm.Type.SMALL)!!.filter { it.count > 6 && it.count < 12 }.count()
                se = bbset.get(Vr15Lottery.Sm.Type.SMALL)!!.filter { it.count >= 12 }.count()
                counts.addAll(res.bbset.map { it.count })
                println("百 -> 大： ${br} > ${be} -  小： ${sr} > ${se}")
                val bdset = res.bdset.stream().sorted(compareByDescending { it.count }).toList().groupBy { it.type }.toMap()
                br = bdset.get(Vr15Lottery.Sm.Type.DOUBLE)!!.filter { it.count > 6 && it.count < 12 }.count()
                be = bdset.get(Vr15Lottery.Sm.Type.DOUBLE)!!.filter { it.count >= 12 }.count()
                sr = bdset.get(Vr15Lottery.Sm.Type.SINGLE)!!.filter { it.count > 6 && it.count < 12 }.count()
                se = bdset.get(Vr15Lottery.Sm.Type.SINGLE)!!.filter { it.count >= 12 }.count()
                counts.addAll(res.bdset.map { it.count })
                println("百 -> 双： ${br} > ${be} -  单： ${sr} > ${se}")

                val sbset = res.sbset.stream().sorted(compareByDescending { it.count }).toList().groupBy { it.type }.toMap()
                br = sbset.get(Vr15Lottery.Sm.Type.BIG)!!.filter { it.count > 6 && it.count < 12 }.count()
                be = sbset.get(Vr15Lottery.Sm.Type.BIG)!!.filter { it.count >= 12 }.count()
                sr = sbset.get(Vr15Lottery.Sm.Type.SMALL)!!.filter { it.count > 6 && it.count < 12 }.count()
                se = sbset.get(Vr15Lottery.Sm.Type.SMALL)!!.filter { it.count >= 12 }.count()
                counts.addAll(res.sbset.map { it.count })
                println("十 -> 大： ${br} > ${be} -  小： ${sr} > ${se}")
                val sdset = res.sdset.stream().sorted(compareByDescending { it.count }).toList().groupBy { it.type }.toMap()
                br = sdset.get(Vr15Lottery.Sm.Type.DOUBLE)!!.filter { it.count > 6 && it.count < 12 }.count()
                be = sdset.get(Vr15Lottery.Sm.Type.DOUBLE)!!.filter { it.count >= 12 }.count()
                sr = sdset.get(Vr15Lottery.Sm.Type.SINGLE)!!.filter { it.count > 6 && it.count < 12 }.count()
                se = sdset.get(Vr15Lottery.Sm.Type.SINGLE)!!.filter { it.count >= 12 }.count()
                counts.addAll(res.sdset.map { it.count })
                println("十 -> 双： ${br} > ${be} -  单： ${sr} > ${se}")


                val gbset = res.gbset.stream().sorted(compareByDescending { it.count }).toList().groupBy { it.type }.toMap()
                br = gbset.get(Vr15Lottery.Sm.Type.BIG)!!.filter { it.count > 6 && it.count < 12 }.count()
                be = gbset.get(Vr15Lottery.Sm.Type.BIG)!!.filter { it.count >= 12 }.count()
                sr = gbset.get(Vr15Lottery.Sm.Type.SMALL)!!.filter { it.count > 6 && it.count < 12 }.count()
                se = gbset.get(Vr15Lottery.Sm.Type.SMALL)!!.filter { it.count >= 12 }.count()
                counts.addAll(res.gbset.map { it.count })
                println("个 -> 大： ${br} > ${be} -  小： ${sr} > ${se}")
                val gdset = res.gdset.stream().sorted(compareByDescending { it.count }).toList().groupBy { it.type }.toMap()
                br = gdset.get(Vr15Lottery.Sm.Type.DOUBLE)!!.filter { it.count > 6 && it.count < 12 }.count()
                be = gdset.get(Vr15Lottery.Sm.Type.DOUBLE)!!.filter { it.count >= 12 }.count()
                sr = gdset.get(Vr15Lottery.Sm.Type.SINGLE)!!.filter { it.count > 6 && it.count < 12 }.count()
                se = gdset.get(Vr15Lottery.Sm.Type.SINGLE)!!.filter { it.count >= 12 }.count()
                counts.addAll(res.gdset.map { it.count })
                println("个 -> 双： ${br} > ${be} -  单： ${sr} > ${se}")
            }
        }

        println(" --------------> " + counts.sortedByDescending { it }.subList(0, 10))
    }
}