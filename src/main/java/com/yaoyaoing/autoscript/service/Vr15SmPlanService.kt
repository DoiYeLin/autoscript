package com.yaoyaoing.autoscript.service

import cn.hutool.core.util.IdUtil
import cn.hutool.core.util.RandomUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yaoyaoing.autoscript.apiargs.Vr15PlanArgs
import com.yaoyaoing.autoscript.common.Keys
import com.yaoyaoing.autoscript.common.RedisService
import com.yaoyaoing.autoscript.common.Ri
import com.yaoyaoing.autoscript.dao.BetPlanDao
import com.yaoyaoing.autoscript.dto.*
import com.yaoyaoing.autoscript.entitys.BetPlan
import com.yaoyaoing.autoscript.entitys.BetRecord
import com.yaoyaoing.autoscript.entitys.OutAccount
import com.yaoyaoing.autoscript.entitys.SscLottery
import com.yaoyaoing.autoscript.utils.MyUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
open class Vr15SmPlanService {

    companion object {
        private val log = LoggerFactory.getLogger(Vr15SmPlanService::class.java)
    }

    @Autowired
    private lateinit var dao: BetPlanDao

    @Autowired
    private lateinit var outAccountService: OutAccountService

    @Autowired
    private lateinit var betrecordService: BetRecordService

    @Autowired
    lateinit var vr15SmPlanZeroService: Vr15SmPlanZeroService

    @Autowired
    lateinit var vr15SmPlanOneService: Vr15SmPlanOneService

    @Autowired
    private lateinit var redisService: RedisService

    open fun getkeys(): Ri<Any> {
        val plans = listOf<Any>(
                mapOf<String, Any>("key" to 0, "name" to "6.11"),
                mapOf<String, Any>("key" to 1, "name" to "autoEssay"),
                mapOf<String, Any>(
                        "key" to 2,
                        "name" to "1~5~1"
                )
        )
        return Ri.successData(plans)
    }

    open fun chidKeys(key: Int): Ri<Any> {
        return Ri.successData(
                when (key) {
                    2 -> {
                        mapOf<String, Any>(
                                "picks" to listOf(
                                        mapOf(
                                                "name" to "ds",
                                                "label" to "单双",
                                                "items" to listOf<Any>(
                                                        mapOf("name" to "h_bs", "label" to "和"),
                                                        mapOf("name" to "w_bs", "label" to "万"),
                                                        mapOf("name" to "q_bs", "label" to "千"),
                                                        mapOf("name" to "b_bs", "label" to "百"),
                                                        mapOf("name" to "s_bs", "label" to "十"),
                                                        mapOf("name" to "g_bs", "label" to "个")
                                                )
                                        ),
                                        mapOf(
                                                "name" to "bs",
                                                "label" to "大小",
                                                "items" to listOf<Any>(
                                                        mapOf("name" to "h_ds", "label" to "和"),
                                                        mapOf("name" to "w_ds", "label" to "万"),
                                                        mapOf("name" to "q_ds", "label" to "千"),
                                                        mapOf("name" to "b_ds", "label" to "百"),
                                                        mapOf("name" to "s_ds", "label" to "十"),
                                                        mapOf("name" to "g_ds", "label" to "个")
                                                )
                                        )
                                ),
                                "desc" to "投注开始位置,选中几个需要几个账号"
                        )

                    }
                    else -> null
                }
        )
    }

    /**
     * 保存计划
     */
    open fun saveSmPlan(plan: Vr15PlanArgs): Ri<Any> {
        if (plan.key < 0) {
            return Ri.error("方案参数错误")
        }
        if (plan.name.isNullOrEmpty()) {
            return Ri.error("方案标签错误")
        }
        if (plan.plans.isNullOrEmpty()) {
            return Ri.error("投注方案参数错误")
        }
        if (plan.odds == null) {
            return Ri.error("赔率")
        }
        val type = object : TypeToken<List<PlanStepDto>>() {}.type

        val plans = Gson().fromJson<List<PlanStepDto>>(plan.plans, type)

        val errs = plans.stream().filter { it.money <= 0 }.count()
        if (errs > 0) {
            return Ri.error("投注方案参数错误")
        }

        plans.forEach(Consumer { kotlin.run { it.failJump-- } })

        val bean = BetPlan()
        bean.game = Keys.Vr15.key
        bean.playMode = Keys.Vr15.sm
        bean.key = plan.key
        bean.name = plan.name
        bean.value = plan.plans
        bean.accNum = plan.accNum
        bean.odds = plan.odds
        bean.createAt = MyUtils.secMillis()

        var res: Int
        if (plan.id.isNullOrEmpty()) {
            bean.id = IdUtil.objectId()
            res = dao.insertSelective(bean)
        } else {
            bean.id = plan.id
            res = dao.updateByPrimaryKeySelective(bean)

        }
        if (res > 0) {
            var dto = PlanDto(bean.id!!, bean.game!!, bean.playMode!!, bean.key!!, bean.name!!, bean.accNum!!)
            dto.step = plans
            redisService.hSet(Keys.Vr15.rk_sm_plans, bean.id!!, dto)
            return Ri.success()
        }
        return Ri.error()
    }

    /**
     * 删除方案
     */
    open fun del(id: String): Ri<Any> {
        val plan: BetPlan? = dao.selectByPrimaryKey(id)
        if (plan != null) {
            if (plan.simulate!!) {
                Plan.Sim.vr15.remove(id)
            }
            if (plan.runing!!) {
                Plan.Run.vr15.remove(id)
            }
            redisService.hDel(Keys.Vr15.rk_sm_plans, id)
            dao.deleteByPrimaryKey(id)
            return Ri.success()
        }
        return Ri.error("无法找到方案")
    }

    /**
     * 系统启动后初始化
     */
    open fun init(plans: List<BetPlan>) {
        for (plan in plans) {
            val dto = PlanDto.convert(plan)
            redisService.hSet(Keys.Vr15.rk_sm_plans, plan.id!!, dto)
            // 方案开启了模拟
            if (plan.simulate!!) {
                Plan.Sim.vr15.add(dto.id)
            }
            if (plan.runing!!) {
                Plan.Run.vr15.add(dto.id)
            }
        }
    }

    /**
     * 方案模拟下注
     */
    open fun simAction(id: String, picks: String?, open: Boolean): Ri<Any> {
//        if (picks.isNullOrEmpty() && open) {
//            return Ri.error("下注单位缺失")
//        }
        val planJson = redisService.hGet(Keys.Vr15.rk_sm_plans, id)
        if (planJson != null) {
            var bean = BetPlan()
            bean.id = id;
            bean.simulate = open
            if (!picks.isNullOrEmpty()) {
                bean.pickMode = picks
            }
            val ups = dao.updateByPrimaryKeySelective(bean)
            if (ups > 0) {
                val sm = Gson().fromJson(Gson().toJson(planJson), PlanDto::class.java)
                sm.simulate = open
                if (!open) {
                    outAccountService.deliverAcc(id)
                }
                sm.pickMode = bean.pickMode
                redisService.hSet(Keys.Vr15.rk_sm_plans, id, sm)
                return Ri.success()
            }
        }
        return Ri.error()
    }

    /**
     * 方案真实运行
     */
    open fun runAction(id: String, picks: String?, open: Boolean): Ri<Any> {
        if (picks.isNullOrEmpty() && open) {
            return Ri.error("下注单位缺失")
        }
        val planJson = redisService.hGet(Keys.Vr15.rk_sm_plans, id)
        if (planJson != null) {
            var bean = BetPlan()
            bean.id = id;
            bean.runing = open
            bean.pickMode = picks
            val ups = dao.updateByPrimaryKeySelective(bean)
            if (ups > 0) {
                val run = Gson().fromJson(Gson().toJson(planJson), PlanDto::class.java)
                run.runing = open
//                if (open) {
//                    Plan.Run.vr15.add(id)
//                } else {
//                    Plan.Run.vr15.remove(id)
//                }
                run.pickMode = bean.pickMode
                redisService.hSet(Keys.Vr15.rk_sm_plans, id, run)
                return Ri.success()
            }
        }
        return Ri.error()
    }

    /**
     * 开奖了
     * @param lot 开奖结果
     * @param args 缓存的进N期开奖号码
     */
//    @Async(value = "scheduledPoolTaskExecutor")
    open fun lottery(lot: SscLottery, args: MutableList<List<Int>>) {
        val smpath = this.analysisPath(args)
        val nextIssue = (lot.issueNo!!.toLong() + 1).toString()
        val plans = redisService.hGetAll(Keys.Vr15.rk_sm_plans)
        plans.forEach({
            val smp = Gson().fromJson(Gson().toJson(it.value), PlanDto::class.java)
            when (smp.key) {
                0 -> plan0(lot, args, nextIssue, smp, smpath)
                1 -> plan1(lot, args, nextIssue, smp, smpath)
                2 -> plan2(lot, args, nextIssue, smp, smpath)
            }
        })
    }

    /**
     * 反投方案
     */
    open fun plan0(
            lot: SscLottery,
            args: MutableList<List<Int>>,
            nextIssue: String,
            plan: PlanDto,
            path: Vr15Lottery.Sm
    ) {
        if (plan.runing!! || plan.simulate!!) {
            var brs: MutableList<BetRecord> = mutableListOf()
            vr15SmPlanZeroService.runbs(lot, plan, path.hbset[0], Keys.Vr15.Unit.sum, nextIssue)
            vr15SmPlanZeroService.runds(lot, plan, path.hdset[0], Keys.Vr15.Unit.sum, nextIssue)

            vr15SmPlanZeroService.runbs(lot, plan, path.wbset[0], Keys.Vr15.Unit.wan, nextIssue)
            vr15SmPlanZeroService.runds(lot, plan, path.wdset[0], Keys.Vr15.Unit.wan, nextIssue)

            vr15SmPlanZeroService.runbs(lot, plan, path.qbset[0], Keys.Vr15.Unit.qian, nextIssue)
            vr15SmPlanZeroService.runds(lot, plan, path.qdset[0], Keys.Vr15.Unit.qian, nextIssue)

            vr15SmPlanZeroService.runbs(lot, plan, path.bbset[0], Keys.Vr15.Unit.bai, nextIssue)
            vr15SmPlanZeroService.runds(lot, plan, path.bdset[0], Keys.Vr15.Unit.bai, nextIssue)

            vr15SmPlanZeroService.runbs(lot, plan, path.sbset[0], Keys.Vr15.Unit.shi, nextIssue)
            vr15SmPlanZeroService.runds(lot, plan, path.sdset[0], Keys.Vr15.Unit.shi, nextIssue)

            vr15SmPlanZeroService.runbs(lot, plan, path.gbset[0], Keys.Vr15.Unit.ge, nextIssue)
            vr15SmPlanZeroService.runds(lot, plan, path.gdset[0], Keys.Vr15.Unit.ge, nextIssue)


        }
    }

    open fun plan1(
            lot: SscLottery,
            args: MutableList<List<Int>>,
            nextIssue: String,
            plan: PlanDto,
            path: Vr15Lottery.Sm
    ) {
        if (plan.runing!! || plan.simulate!!) {
            vr15SmPlanOneService.run(lot, plan, path, Keys.Vr15.Unit.sum, nextIssue)
            vr15SmPlanOneService.run(lot, plan, path, Keys.Vr15.Unit.wan, nextIssue)
            vr15SmPlanOneService.run(lot, plan, path, Keys.Vr15.Unit.qian, nextIssue)
            vr15SmPlanOneService.run(lot, plan, path, Keys.Vr15.Unit.bai, nextIssue)
            vr15SmPlanOneService.run(lot, plan, path, Keys.Vr15.Unit.shi, nextIssue)
            vr15SmPlanOneService.run(lot, plan, path, Keys.Vr15.Unit.ge, nextIssue)
        }
    }

    /**
     * 方案2开奖核对
     * 单双/大小 的 总和跟每个数据以此投注
     * @param lot 开奖结果
     * @param args 缓存的进N期开奖号码
     * @param issue 下期投注期号
     * @param path 缓存的开奖号码 路单
     */
    open fun plan2(
            lot: SscLottery,
            args: MutableList<List<Int>>?,
            issue: String,
            plan: PlanDto,
            path: Vr15Lottery.Sm?
    ) {
        if (!plan.runing!! && !plan.simulate!!) {
            return
        }
        var pms = plan.pickMode!!.split(",")
        for (item in pms) {
//            val rko = "vr15-sm-sim2-${item}-${lot.issueNo}"
//            val rkn = "vr15-sm-sim2-${item}-${issue}"
            val rko = "vr15-sm-betlast-${item}"
//            val rkn = "vr15-sm-betnew-${item}"

            // 投注账号
            var account: OutAccount? = null
            // 下注目标，大小 / 单双
            var betTarget: String? = null
            // 方案跟投下标
            var step: Int = 0
            // 跟投次数, 同单位投注两次不中就换
            var follow: Int = 1
            // 连续未中次数
            var noLotLen: Int = 0
            // 跳过本期投注
            var jump = false


            var betTargetPick: Vr15Lottery.Sm.Type? = null

            // 选中的投注单位索引
            var pickMode: Int? = null
            // 选中的投注单位
            var pickModeVal: Int?

            var lastBr: BetRecord? = null
            /**
             * 上期投注开奖核对
             */
            if (redisService.hHasKey(Keys.rk_bet_picks, rko)) {
                val lastbet = redisService.hGet(Keys.rk_bet_picks, rko)
                lastBr = Gson().fromJson(Gson().toJson(lastbet), BetRecord::class.java)
                account = outAccountService.grabAcc(lastBr.accId!!, plan.id)

                /**
                 * 下注号码开奖结果核对
                 * 大小
                 * 单双
                 */
                var openTargetPick: Vr15Lottery.Sm.Type? = null

                /**
                 * 切换投注单位
                 * 命中 / N次跟投未中
                 */
                val changeBetUnit = kotlin.run {
                    pickMode = lastBr.pickMode?.plus(1)
//                    if (item.contains(Keys.Vr15.BS)) {
//                        betTarget = Keys.Vr15.BS
//                        betTargetPick = Vr15Lottery.Sm.Type.BIG
//                    }
//                    if (item.contains(Keys.Vr15.DS)) {
//                        betTarget = Keys.Vr15.DS
//                        betTargetPick = Vr15Lottery.Sm.Type.DOUBLE
//                    }

                    if (item.contains(Keys.Vr15.BS)) {
                        betTarget = Keys.Vr15.BS
                        if (lastBr.betTargetPick.equals(Vr15Lottery.Sm.Type.BIG.toString())) {
                            betTargetPick = Vr15Lottery.Sm.Type.SMALL
                        } else {
                            betTargetPick = Vr15Lottery.Sm.Type.BIG
                        }
                    }
                    if (item.contains(Keys.Vr15.DS)) {
                        betTarget = Keys.Vr15.DS
                        if (lastBr.betTargetPick.equals(Vr15Lottery.Sm.Type.DOUBLE.toString())) {
                            betTargetPick = Vr15Lottery.Sm.Type.SINGLE
                        } else {
                            betTargetPick = Vr15Lottery.Sm.Type.DOUBLE
                        }
                    }
                }


                /**
                 * 大小分界数字，和是22，其他单位是4
                 */
                var bs_divide: Int?
                // 投注单位索引 = 0 表示是 和
                if (lastBr.pickMode == 0) {
                    val nums = lot.betNumber!!.map(Character::getNumericValue).toList()
                    pickModeVal = nums.sum()
                    bs_divide = Keys.Vr15.hebs_divide
                } else {
                    pickModeVal = lastBr.pickMode?.let { Character.getNumericValue(lot.betNumber!!.get(it - 1)) }
                    bs_divide = 4
                }

                if (pickModeVal != null) {
                    if (item.contains(Keys.Vr15.BS)) {
                        if (pickModeVal <= bs_divide) {
                            openTargetPick = Vr15Lottery.Sm.Type.SMALL
                        } else {
                            openTargetPick = Vr15Lottery.Sm.Type.BIG
                        }
                    }
                    if (item.contains(Keys.Vr15.DS)) {
                        if (pickModeVal % 2 == 0) {
                            openTargetPick = Vr15Lottery.Sm.Type.DOUBLE
                        } else {
                            openTargetPick = Vr15Lottery.Sm.Type.SINGLE
                        }
                    }

                    var brBean = BetRecord()
                    if (lastBr.betTargetPick!!.equals(openTargetPick.toString())) {
                        // 中了, 跳到下个单位，重新开始
                        changeBetUnit
                        brBean.id = lastBr.id;
                        brBean.number = lot.betNumber
                        brBean.status = 1

                        if (plan.simulate!!) {
                            outAccountService.simMoneyAdd(lastBr.accId!!, lastBr.betMoney!! * plan.odds!!)
                        }
                        if (plan.runing!!) {
                            outAccountService.moneyAdd(lastBr.accId!!, lastBr.betMoney!! * plan.odds!!)
                        }
                    } else {
                        noLotLen = lastBr.noLotLen?.plus(1)!!
                        step = lastBr.step?.plus(1)!!
                        if (noLotLen == Keys.betPauseLen) {
                            jump = true
                        } else if (noLotLen > Keys.betPauseLen) {
                            if (RandomUtil.randomBoolean()) {
                                jump = true
                            }
                        }

                        // 同单位最多跟投次数
                        if (lastBr.follow!! < 2) {
                            // 没中, 跟投一把
//                            if (RandomUtil.randomBoolean()) {
//                                pickMode = lastBr.pickMode
//                            }
                            pickMode = lastBr.pickMode!!.plus(1)
                            follow = lastBr.follow?.plus(1)!!

                            if (item.contains(Keys.Vr15.BS)) {
                                betTarget = Keys.Vr15.BS
                                if (lastBr.betTargetPick.equals(Vr15Lottery.Sm.Type.BIG.toString())) {
                                    betTargetPick = Vr15Lottery.Sm.Type.BIG
                                } else {
                                    betTargetPick = Vr15Lottery.Sm.Type.SMALL
                                }
                            }
                            if (item.contains(Keys.Vr15.DS)) {
                                betTarget = Keys.Vr15.DS
                                if (lastBr.betTargetPick.equals(Vr15Lottery.Sm.Type.DOUBLE.toString())) {
                                    betTargetPick = Vr15Lottery.Sm.Type.DOUBLE
                                } else {
                                    betTargetPick = Vr15Lottery.Sm.Type.SINGLE
                                }
                            }
                        } else {
                            changeBetUnit
                        }
                        brBean.id = lastBr.id;
                        brBean.number = lot.betNumber
                        brBean.status = 2
                    }

                    betrecordService.updateByPrimaryKeySelective(brBean)
                    // 删除上期投注缓存
                    redisService.hDel(Keys.rk_bet_picks, rko)
                }
                /**
                 * 释放账号
                 */
                if (!plan.simulate!! || !plan.runing!!) {
                    lastBr.accId?.let { outAccountService.deliverAcc(it) }
                }
            } else if (plan.simulate!!) {
                /**
                 * 上期没有投注，重新开始投注
                 */
                if (item.contains(Keys.Vr15.BS)) {
                    betTarget = Keys.Vr15.BS
                    betTargetPick = Vr15Lottery.Sm.Type.BIG
                    pickMode = Keys.Vr15.sm_targets.get(Keys.Vr15.BS)!!.indexOf(item)
                }
                if (item.contains(Keys.Vr15.DS)) {
                    betTarget = Keys.Vr15.DS
                    betTargetPick = Vr15Lottery.Sm.Type.DOUBLE
                    pickMode = Keys.Vr15.sm_targets.get(Keys.Vr15.DS)!!.indexOf(item)
                }


                if (redisService.hHasKey(Keys.rk_common_map, Keys.Vr15.rk_vr15_betpause + item)) {
                    val bpJson = redisService.hGet(Keys.rk_common_map, Keys.Vr15.rk_vr15_betpause + item)
                    val bp = Gson().fromJson(Gson().toJson(bpJson), BetPause::class.java)
                    noLotLen = bp.noLotLen.plus(1)
                    step = bp.step.plus(1)
                    follow = bp.follow.plus(1)
                    pickMode = bp.pickMode.plus(1)
                    account = outAccountService.grabAcc(bp.account, bp.planId)
                    redisService.hDel(Keys.rk_common_map, Keys.Vr15.rk_vr15_betpause + item)

                    if (item.contains(Keys.Vr15.BS)) {
                        if (bp.ext.equals(Vr15Lottery.Sm.Type.BIG.toString())) {
                            betTargetPick = Vr15Lottery.Sm.Type.BIG
                        } else {
                            betTargetPick = Vr15Lottery.Sm.Type.SMALL
                        }
                    }
                    if (item.contains(Keys.Vr15.DS)) {
                        if (bp.ext.equals(Vr15Lottery.Sm.Type.DOUBLE.toString())) {
                            betTargetPick = Vr15Lottery.Sm.Type.DOUBLE
                        } else {
                            betTargetPick = Vr15Lottery.Sm.Type.SINGLE
                        }
                    }
                } else {
                    if (plan.simulate!!) {
                        account = outAccountService.grabAcc(OutAccount.Type.simulate.code, plan.id)!!
                    }
                    if (plan.runing!!) {
                        account = outAccountService.grabAcc(OutAccount.Type.runing.code, plan.id)!!
                    }
                }
            }

            if (step > plan.step!!.size - 1) {
                step = 0
            }
            if (pickMode?.compareTo(5)!! > 0) {
                pickMode = 0
            }

            if ((plan.simulate!! || plan.runing!!) && !lot.issueNo!!.endsWith(Keys.Vr15.max_issue.toString())) {
                if (!jump) {
                    var br = BetRecord()
                    br.id = IdUtil.objectId()
                    br.game = Keys.Vr15.key
                    br.accId = account!!.id
                    br.planId = plan.id
                    br.playMode = Keys.Vr15.sm
                    br.pickMode = pickMode
                    br.betTarget = betTarget
                    br.betTargetStart = item
                    br.betTargetPick = betTargetPick.toString()
                    br.issueNo = issue
                    br.type = Keys.Vr15.sim
                    br.betMoney = plan.step!!.get(step).money
                    br.status = 0
                    br.createAt = MyUtils.secMillis()
                    br.step = step
                    br.follow = follow
                    br.noLotLen = noLotLen

                    val ins = betrecordService.insertSelective(br)
                    if (ins > 0) {
                        if (plan.simulate!!) {
                            outAccountService.simMoneySub(br.accId!!, br.betMoney!!)
                        }
                        if (plan.runing!!) {
                            outAccountService.moneySub(br.accId!!, br.betMoney!!)
                        }
                        redisService.hSet(Keys.rk_bet_picks, rko, br)
                    }
                } else {
                    val bp = BetPause(
                            lastBr?.issueNo!!,
                            lastBr.pickMode!!,
                            lastBr.step!!,
                            noLotLen,
                            lastBr.noLotLen!!,
                            lastBr.accId!!,
                            lastBr.planId!!,
                            lastBr.betTargetPick!!
                    )
                    redisService.hSet(Keys.rk_common_map, Keys.Vr15.rk_vr15_betpause + item, bp)
                }
            }
        }
    }

    /**
     * 分析开奖号路单
     * @param args 缓存中N期开奖号
     */
    open fun analysisPath(args: List<List<Int>>): Vr15Lottery.Sm {
        // 和，单双，大小
        var hbSet = mutableListOf<Vr15Lottery.Sm.Path>()
        var hdSet = mutableListOf<Vr15Lottery.Sm.Path>()
        // 万，单双，大小
        var wbSet = mutableListOf<Vr15Lottery.Sm.Path>()
        var wdSet = mutableListOf<Vr15Lottery.Sm.Path>()
        // 千，单双，大小
        var qbSet = mutableListOf<Vr15Lottery.Sm.Path>()
        var qdSet = mutableListOf<Vr15Lottery.Sm.Path>()
        // 百，单双，大小
        var bbSet = mutableListOf<Vr15Lottery.Sm.Path>()
        var bdSet = mutableListOf<Vr15Lottery.Sm.Path>()
        // 十，单双，大小
        var sbSet = mutableListOf<Vr15Lottery.Sm.Path>()
        var sdSet = mutableListOf<Vr15Lottery.Sm.Path>()
        // 个，单双，大小
        var gbSet = mutableListOf<Vr15Lottery.Sm.Path>()
        var gdSet = mutableListOf<Vr15Lottery.Sm.Path>()

        var hbs: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.BIG, 0)
        var hds: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.DOUBLE, 0)

        var wbs: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.BIG, 0)
        var wds: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.DOUBLE, 0)

        var qbs: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.BIG, 0)
        var qds: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.DOUBLE, 0)

        var bbs: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.BIG, 0)
        var bds: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.DOUBLE, 0)

        var sbs: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.BIG, 0)
        var sds: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.DOUBLE, 0)

        var gbs: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.BIG, 0)
        var gds: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.DOUBLE, 0)

        for (arg in args) {
            val he = arg.sum()

            var type: Vr15Lottery.Sm.Type
            if (he <= Keys.Vr15.hebs_divide) {
                type = Vr15Lottery.Sm.Type.SMALL
            } else {
                type = Vr15Lottery.Sm.Type.BIG
            }
            if (hbs.type.equals(type)) {
                hbs.count++
            } else {
                if (hbs.count > 0) hbSet.add(hbs)
                hbs = Vr15Lottery.Sm.Path(type, 1)
            }

            if (he % 2 == 0) {
                type = Vr15Lottery.Sm.Type.DOUBLE
            } else {
                type = Vr15Lottery.Sm.Type.SINGLE
            }
            if (hds.type.equals(type)) {
                hds.count++
            } else {
                if (hds.count > 0) hdSet.add(hds)
                hds = Vr15Lottery.Sm.Path(type, 1)
            }


            val w = arg.get(0)
            if (w <= 4) {
                type = Vr15Lottery.Sm.Type.SMALL
            } else {
                type = Vr15Lottery.Sm.Type.BIG
            }
            if (wbs.type.equals(type)) {
                wbs.count++
            } else {
                if (wbs.count > 0) wbSet.add(wbs)
                wbs = Vr15Lottery.Sm.Path(type, 1)
            }

            if (w % 2 == 0) {
                type = Vr15Lottery.Sm.Type.DOUBLE
            } else {
                type = Vr15Lottery.Sm.Type.SINGLE
            }
            if (wds.type.equals(type)) {
                wds.count++
            } else {
                if (wds.count > 0) wdSet.add(wds)
                wds = Vr15Lottery.Sm.Path(type, 1)
            }


            val q = arg.get(1)
            if (q <= 4) {
                type = Vr15Lottery.Sm.Type.SMALL
            } else {
                type = Vr15Lottery.Sm.Type.BIG
            }
            if (qbs.type.equals(type)) {
                qbs.count++
            } else {
                if (qbs.count > 0) qbSet.add(qbs)
                qbs = Vr15Lottery.Sm.Path(type, 1)
            }

            if (q % 2 == 0) {
                type = Vr15Lottery.Sm.Type.DOUBLE
            } else {
                type = Vr15Lottery.Sm.Type.SINGLE
            }
            if (qds.type.equals(type)) {
                qds.count++
            } else {
                if (qds.count > 0) qdSet.add(qds)
                qds = Vr15Lottery.Sm.Path(type, 1)
            }


            val b = arg.get(2)
            if (b <= 4) {
                type = Vr15Lottery.Sm.Type.SMALL
            } else {
                type = Vr15Lottery.Sm.Type.BIG
            }
            if (bbs.type.equals(type)) {
                bbs.count++
            } else {
                if (bbs.count > 0) bbSet.add(bbs)
                bbs = Vr15Lottery.Sm.Path(type, 1)
            }

            if (b % 2 == 0) {
                type = Vr15Lottery.Sm.Type.DOUBLE
            } else {
                type = Vr15Lottery.Sm.Type.SINGLE
            }
            if (bds.type.equals(type)) {
                bds.count++
            } else {
                if (bds.count > 0) bdSet.add(bds)
                bds = Vr15Lottery.Sm.Path(type, 1)
            }


            val s = arg.get(3)
            if (s <= 4) {
                type = Vr15Lottery.Sm.Type.SMALL
            } else {
                type = Vr15Lottery.Sm.Type.BIG
            }
            if (sbs.type.equals(type)) {
                sbs.count++
            } else {
                if (sbs.count > 0) sbSet.add(sbs)
                sbs = Vr15Lottery.Sm.Path(type, 1)
            }

            if (s % 2 == 0) {
                type = Vr15Lottery.Sm.Type.DOUBLE
            } else {
                type = Vr15Lottery.Sm.Type.SINGLE
            }
            if (sds.type.equals(type)) {
                sds.count++
            } else {
                if (sds.count > 0) sdSet.add(sds)
                sds = Vr15Lottery.Sm.Path(type, 1)
            }


            val g = arg.get(4)
            if (g <= 4) {
                type = Vr15Lottery.Sm.Type.SMALL
            } else {
                type = Vr15Lottery.Sm.Type.BIG
            }
            if (gbs.type.equals(type)) {
                gbs.count++
            } else {
                if (gbs.count > 0) gbSet.add(gbs)
                gbs = Vr15Lottery.Sm.Path(type, 1)
            }

            if (g % 2 == 0) {
                type = Vr15Lottery.Sm.Type.DOUBLE
            } else {
                type = Vr15Lottery.Sm.Type.SINGLE
            }
            if (gds.type.equals(type)) {
                gds.count++
            } else {
                if (gds.count > 0) gdSet.add(gds)
                gds = Vr15Lottery.Sm.Path(type, 1)
            }
        }

        if (hbSet.size == 0) {
            hbSet.add(hbs)
        }
        if (hdSet.size == 0) {
            hdSet.add(hds)
        }
        if (wbSet.size == 0) {
            wbSet.add(wbs)
        }
        if (wdSet.size == 0) {
            wdSet.add(wds)
        }
        if (qbSet.size == 0) {
            qbSet.add(qbs)
        }
        if (qdSet.size == 0) {
            qdSet.add(qds)
        }
        if (bbSet.size == 0) {
            bbSet.add(bbs)
        }
        if (bdSet.size == 0) {
            bdSet.add(bds)
        }
        if (sbSet.size == 0) {
            sbSet.add(sbs)
        }
        if (sdSet.size == 0) {
            sdSet.add(sds)
        }
        if (gbSet.size == 0) {
            gbSet.add(gbs)
        }
        if (gdSet.size == 0) {
            gdSet.add(gds)
        }

        var vr15SmPath = Vr15Lottery.Sm()
        vr15SmPath.hbset = hbSet
        vr15SmPath.hdset = hdSet
        vr15SmPath.wbset = wbSet
        vr15SmPath.wdset = wdSet
        vr15SmPath.qbset = qbSet
        vr15SmPath.qdset = qdSet
        vr15SmPath.bbset = bbSet
        vr15SmPath.bdset = bdSet
        vr15SmPath.sbset = sbSet
        vr15SmPath.sdset = sdSet
        vr15SmPath.gbset = gbSet
        vr15SmPath.gdset = gdSet

        return vr15SmPath
    }


    open fun analysisCyclePath(args: List<List<Int>>): Vr15Lottery.Sm {
        // 和，单双，大小
        var hbSet = mutableListOf<Vr15Lottery.Sm.Path>()
        var hdSet = mutableListOf<Vr15Lottery.Sm.Path>()
        // 万，单双，大小
        var wbSet = mutableListOf<Vr15Lottery.Sm.Path>()
        var wdSet = mutableListOf<Vr15Lottery.Sm.Path>()
        // 千，单双，大小
        var qbSet = mutableListOf<Vr15Lottery.Sm.Path>()
        var qdSet = mutableListOf<Vr15Lottery.Sm.Path>()
        // 百，单双，大小
        var bbSet = mutableListOf<Vr15Lottery.Sm.Path>()
        var bdSet = mutableListOf<Vr15Lottery.Sm.Path>()
        // 十，单双，大小
        var sbSet = mutableListOf<Vr15Lottery.Sm.Path>()
        var sdSet = mutableListOf<Vr15Lottery.Sm.Path>()
        // 个，单双，大小
        var gbSet = mutableListOf<Vr15Lottery.Sm.Path>()
        var gdSet = mutableListOf<Vr15Lottery.Sm.Path>()

        var hbs: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.BIG, 0)
        var hds: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.DOUBLE, 0)

        var wbs: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.BIG, 0)
        var wds: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.DOUBLE, 0)

        var qbs: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.BIG, 0)
        var qds: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.DOUBLE, 0)

        var bbs: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.BIG, 0)
        var bds: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.DOUBLE, 0)

        var sbs: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.BIG, 0)
        var sds: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.DOUBLE, 0)

        var gbs: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.BIG, 0)
        var gds: Vr15Lottery.Sm.Path = Vr15Lottery.Sm.Path(Vr15Lottery.Sm.Type.DOUBLE, 0)

        for (arg in args) {
            val he = arg.sum()

            var type: Vr15Lottery.Sm.Type
            if (he <= Keys.Vr15.hebs_divide) {
                type = Vr15Lottery.Sm.Type.SMALL
            } else {
                type = Vr15Lottery.Sm.Type.BIG
            }
            if (hbs.type.equals(type)) {
                hbs.count++
            } else {
                if (hbs.count > 0) hbSet.add(hbs)
                hbs = Vr15Lottery.Sm.Path(type, 1)
            }

            if (he % 2 == 0) {
                type = Vr15Lottery.Sm.Type.DOUBLE
            } else {
                type = Vr15Lottery.Sm.Type.SINGLE
            }
            if (hds.type.equals(type)) {
                hds.count++
            } else {
                if (hds.count > 0) hdSet.add(hds)
                hds = Vr15Lottery.Sm.Path(type, 1)
            }


            val w = arg.get(0)
            if (w <= 4) {
                type = Vr15Lottery.Sm.Type.SMALL
            } else {
                type = Vr15Lottery.Sm.Type.BIG
            }
            if (wbs.type.equals(type)) {
                wbs.count++
            } else {
                if (wbs.count > 0) wbSet.add(wbs)
                wbs = Vr15Lottery.Sm.Path(type, 1)
            }

            if (w % 2 == 0) {
                type = Vr15Lottery.Sm.Type.DOUBLE
            } else {
                type = Vr15Lottery.Sm.Type.SINGLE
            }
            if (wds.type.equals(type)) {
                wds.count++
            } else {
                if (wds.count > 0) wdSet.add(wds)
                wds = Vr15Lottery.Sm.Path(type, 1)
            }


            val q = arg.get(1)
            if (q <= 4) {
                type = Vr15Lottery.Sm.Type.SMALL
            } else {
                type = Vr15Lottery.Sm.Type.BIG
            }
            if (qbs.type.equals(type)) {
                qbs.count++
            } else {
                if (qbs.count > 0) qbSet.add(qbs)
                qbs = Vr15Lottery.Sm.Path(type, 1)
            }

            if (q % 2 == 0) {
                type = Vr15Lottery.Sm.Type.DOUBLE
            } else {
                type = Vr15Lottery.Sm.Type.SINGLE
            }
            if (qds.type.equals(type)) {
                qds.count++
            } else {
                if (qds.count > 0) qdSet.add(qds)
                qds = Vr15Lottery.Sm.Path(type, 1)
            }


            val b = arg.get(2)
            if (b <= 4) {
                type = Vr15Lottery.Sm.Type.SMALL
            } else {
                type = Vr15Lottery.Sm.Type.BIG
            }
            if (bbs.type.equals(type)) {
                bbs.count++
            } else {
                if (bbs.count > 0) bbSet.add(bbs)
                bbs = Vr15Lottery.Sm.Path(type, 1)
            }

            if (b % 2 == 0) {
                type = Vr15Lottery.Sm.Type.DOUBLE
            } else {
                type = Vr15Lottery.Sm.Type.SINGLE
            }
            if (bds.type.equals(type)) {
                bds.count++
            } else {
                if (bds.count > 0) bdSet.add(bds)
                bds = Vr15Lottery.Sm.Path(type, 1)
            }


            val s = arg.get(3)
            if (s <= 4) {
                type = Vr15Lottery.Sm.Type.SMALL
            } else {
                type = Vr15Lottery.Sm.Type.BIG
            }
            if (sbs.type.equals(type)) {
                sbs.count++
            } else {
                if (sbs.count > 0) sbSet.add(sbs)
                sbs = Vr15Lottery.Sm.Path(type, 1)
            }

            if (s % 2 == 0) {
                type = Vr15Lottery.Sm.Type.DOUBLE
            } else {
                type = Vr15Lottery.Sm.Type.SINGLE
            }
            if (sds.type.equals(type)) {
                sds.count++
            } else {
                if (sds.count > 0) sdSet.add(sds)
                sds = Vr15Lottery.Sm.Path(type, 1)
            }


            val g = arg.get(4)
            if (g <= 4) {
                type = Vr15Lottery.Sm.Type.SMALL
            } else {
                type = Vr15Lottery.Sm.Type.BIG
            }
            if (gbs.type.equals(type)) {
                gbs.count++
            } else {
                if (gbs.count > 0) gbSet.add(gbs)
                gbs = Vr15Lottery.Sm.Path(type, 1)
            }

            if (g % 2 == 0) {
                type = Vr15Lottery.Sm.Type.DOUBLE
            } else {
                type = Vr15Lottery.Sm.Type.SINGLE
            }
            if (gds.type.equals(type)) {
                gds.count++
            } else {
                if (gds.count > 0) gdSet.add(gds)
                gds = Vr15Lottery.Sm.Path(type, 1)
            }
        }

        if (hbSet.size == 0) {
            hbSet.add(hbs)
        }
        if (hdSet.size == 0) {
            hdSet.add(hds)
        }
        if (wbSet.size == 0) {
            wbSet.add(wbs)
        }
        if (wdSet.size == 0) {
            wdSet.add(wds)
        }
        if (qbSet.size == 0) {
            qbSet.add(qbs)
        }
        if (qdSet.size == 0) {
            qdSet.add(qds)
        }
        if (bbSet.size == 0) {
            bbSet.add(bbs)
        }
        if (bdSet.size == 0) {
            bdSet.add(bds)
        }
        if (sbSet.size == 0) {
            sbSet.add(sbs)
        }
        if (sdSet.size == 0) {
            sdSet.add(sds)
        }
        if (gbSet.size == 0) {
            gbSet.add(gbs)
        }
        if (gdSet.size == 0) {
            gdSet.add(gds)
        }

        var vr15SmPath = Vr15Lottery.Sm()
        vr15SmPath.hbset = hbSet
        vr15SmPath.hdset = hdSet
        vr15SmPath.wbset = wbSet
        vr15SmPath.wdset = wdSet
        vr15SmPath.qbset = qbSet
        vr15SmPath.qdset = qdSet
        vr15SmPath.bbset = bbSet
        vr15SmPath.bdset = bdSet
        vr15SmPath.sbset = sbSet
        vr15SmPath.sdset = sdSet
        vr15SmPath.gbset = gbSet
        vr15SmPath.gdset = gdSet

        return vr15SmPath
    }


}