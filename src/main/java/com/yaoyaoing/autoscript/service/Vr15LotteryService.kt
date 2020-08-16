package com.yaoyaoing.autoscript.service

import cn.hutool.core.date.DateUtil
import cn.hutool.core.util.IdUtil
import com.alibaba.excel.EasyExcel
import com.alibaba.excel.ExcelWriter
import com.alibaba.excel.write.metadata.fill.FillConfig
import com.github.pagehelper.PageHelper.startPage
import com.yaoyaoing.autoscript.common.Keys
import com.yaoyaoing.autoscript.common.RedisService
import com.yaoyaoing.autoscript.common.Ri
import com.yaoyaoing.autoscript.dao.SscLotteryDao
import com.yaoyaoing.autoscript.dto.ExLuData
import com.yaoyaoing.autoscript.dto.ExSmData
import com.yaoyaoing.autoscript.dto.SscBetDto
import com.yaoyaoing.autoscript.entitys.SscLottery
import com.yaoyaoing.autoscript.utils.MyUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.PathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import tk.mybatis.mapper.entity.Example
import java.nio.file.Paths
import java.util.*
import java.util.function.Consumer
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Service
open class Vr15LotteryService {

    @Autowired
    private lateinit var dao: SscLotteryDao

    @Autowired
    private lateinit var redisService: RedisService

    @Autowired
    private lateinit var vr15SmPlanService: Vr15SmPlanService

    @Autowired
    private lateinit var request: HttpServletRequest

    @Autowired
    private lateinit var response: HttpServletResponse

    @Value("\${cs.file.template}")
    lateinit var template: String

    @Value("\${cs.file.temporary}")
    lateinit var temporary: String

    companion object {
        // 获取开奖结果
        private var gnLock = false
        private val log = LoggerFactory.getLogger(Vr15LotteryService::class.java)
    }

    /**
     * 下注
     */
    open fun bet(sscBetDto: SscBetDto): Ri<Any> {
        var ssc = SscLottery()
        ssc.id = IdUtil.objectId()
        ssc.issueNo = sscBetDto.issue
//        ssc.betMoney = sscBetDto.money
//        ssc.betTarget = sscBetDto.target
        ssc.game = sscBetDto.game
//        ssc.playMode = sscBetDto.playmode
        ssc.createAt = MyUtils.secMillis()
        val ins = dao.insertSelective(ssc)
        if (ins > 0) {
            return Ri.success()
        }
        return Ri.error()
    }

    open fun vr15_open_align(): Ri<Any> {
        gnLock = true
        val start = MyUtils.secMillis()
        val before = redisService.zreverseRangeWithScores(SscLottery.vrjx_15_day_bet_score, 0, 0)
        runBlocking {
            launch { numbers(1, 0) }.join()
        }

        val end = MyUtils.secMillis()
        val after = redisService.zreverseRangeWithScores(SscLottery.vrjx_15_day_bet_score, 0, 0)
        if (after!!.last().score!! > before!!.last().score!!) {
            redisService.zadd(Keys.rk_common_score, Keys.Vr15.rk_open_align, (90 - (end - start)).toDouble())
            gnLock = false
            return Ri.success()
        } else {
            gnLock = false
        }
        return Ri.error("开奖后几秒后校准")
    }

    /**
     * 开奖记录
     */
    @Synchronized
    open fun openNumber(numberInx: Int, ssc: SscBetDto): Ri<Any> {
        if (ssc.issue!!.endsWith(Keys.Vr15.max_issue.toString())) {
            val cal = Calendar.getInstance()
            if (!redisService.hasKey(Keys.Vr15.rk_adjourn) && cal.get(Calendar.HOUR_OF_DAY) < 9) {
                cal.set(Calendar.HOUR_OF_DAY, 9)
                cal.set(Calendar.MINUTE, 2)
                cal.set(Calendar.SECOND, 0)
                val open = cal.timeInMillis / 1000 - MyUtils.secMillis()
                redisService.set(Keys.Vr15.rk_adjourn, true, open)
//                redisService.zadd(Keys.rk_common_score, Keys.Vr15.rk_open_align, 2.0)
                DateUtil.format(cal.time, MyUtils.date_number)
            } else {
                return Ri.warning("")
            }
        }
        if (!ssc.issue.isNullOrEmpty() && !ssc.number.isNullOrEmpty()) {
            if (!redisService.hHasKey(SscLottery.vrjx_15_day_bet, ssc.issue!!)) {
                redisService.hSet(SscLottery.vrjx_15_day_bet, ssc.issue!!, ssc.number!!)
                redisService.zadd(SscLottery.vrjx_15_day_bet_score, ssc.number!!, ssc.issue!!.toDouble())
                val lottery = this.saveNumber(ssc)

                val history = redisService.zrevrange(SscLottery.vrjx_15_day_bet_score, 0, Keys.cacheNumberSize)

                var numbers = mutableListOf<List<Int>>()
                for (item in history) {
                    val nums = item.toString().map(Character::getNumericValue).toList()
                    numbers.add(nums)
                }
                /**
                 * 获取开奖结果，只计算第一次入库的结果
                 */
                if (numberInx == 0 && lottery != null) {
//                    vr15SmPlanService.lottery(lottery, numbers)
                }
                return Ri.success()
            }
        }
        return Ri.error()
    }

    open fun saveNumber(ssc: SscBetDto): SscLottery? {
        var ex = Example(SscLottery::class.java)
        ex.createCriteria().andEqualTo(SscLottery.ISSUE_NO, ssc.issue)

        var bean = SscLottery()
        bean.id = IdUtil.objectId()
        bean.game = ssc.game
        bean.issueNo = ssc.issue
        bean.betNumber = ssc.number
        bean.createAt = MyUtils.secMillis()
        val res = dao.insertSelective(bean)


        val remSet = redisService.zreverseRangeWithScores(SscLottery.vrjx_15_day_bet_score, Keys.cacheNumberSize, -1)
        if (remSet != null && remSet.size > 0) {
            redisService.zremRangeByScore(SscLottery.vrjx_15_day_bet_score, remSet.last().score!!, remSet.first().score!!)
            remSet.forEach(Consumer { item ->
                redisService.hDel(SscLottery.vrjx_15_day_bet, item.score!!.toLong().toString())
            })

        }
        if (res > 0) {
            return bean
        }
        return null
    }

    /**
     * 分析龙虎
     */
    fun analysisLH(arg: MutableSet<Any>) {
        var res = mutableMapOf<String, Any>()

        var numbers = mutableListOf<List<Int>>()
        for (item in arg) {
            val nums = item.toString().map(Character::getNumericValue).toList()
            numbers.add(nums)
        }

        var wq = mutableMapOf<String, Int>("wq_long" to 0, "wq_hu" to 0, "wq_he" to 0)


        for (item in numbers) {
            if (item.get(0) > item.get(1)) {
                wq["wq_long"] = wq.get("wq_long")!! + 1
            } else if (item.get(0) < item.get(1)) {
                wq["wq_hu"] = wq.get("wq_hu")!! + 1
            } else {
                wq["wq_he"] = wq.get("wq_he")!! + 1
            }
        }
        println(numbers)
    }

    @Async(value = "scheduledPoolTaskExecutor")
    open fun getNumbers(type: Int, cbnum: Int) = runBlocking {
        if (!gnLock) {
            launch {
                numbers(type, cbnum)
            }
        }
    }

    /**
     * type: 0 表示是10秒倒计时获取数据的
     *       1 表示校准获取数据的
     */
    open suspend fun numbers(type: Int, cbnum: Int) {
        var status = -1
        val bcode = "var Con_BonusCode = \""
        try {

            val document: Document? = Jsoup.connect(Keys.Vr15.get_number_url).get()
            if (document != null) {
                val element = document.select("script")
                val numberScript = element.stream().filter { t -> t.data().contains(bcode) }.findAny().orElse(null)
                if (numberScript != null) {
                    var numberText = numberScript.data()
                    val startInx = numberText.indexOf(bcode)
                    numberText = numberText.substring(startInx + bcode.length)
                    numberText = numberText.substring(0, numberText.indexOf("\";"))

                    var numbers = numberText.split(";")
                    val map = mutableMapOf<String, String>()
                    numbers.forEach { item ->
                        run {
                            val openNumber = item.split("=")
                            map.put(openNumber[0], openNumber[1])
                        }
                    }
                    val keys = map.keys.toList().reversed()

                    var inx = -1
                    for (item in keys) {
                        inx++
                        var ssc = SscBetDto()
                        ssc.game = Keys.Vr15.key
                        ssc.issue = item
                        ssc.number = map.get(item)!!.replace(",", "")
                        val res = this.openNumber(inx, ssc)

                        if (inx == 0) {
                            when (res.code) {
                                200 -> status = 1
                                101 -> status = 0
                                else -> {
                                    println("${type} No new data was retrieved")
                                }
                            }
                        }
//                    if (type == 0) {
//                        return
//                    } else {
//                        break
//                    }
                    }
                    println("同步完成")
                }
            }
            if (status == -1 && type != 0 && cbnum < 8) {
                delay(2000L)
                log.error("重复获取开奖结果等待2秒")
                this.getNumbers(type, cbnum + 1)
            }
        } catch (e: Exception) {
            log.error("同步异常", e)
            this.getNumbers(type, cbnum + 1)
        }
    }


    open fun cache(day: String, page: Int): Ri<Any> {
        var res: MutableList<Any>?

//        val calendar = Calendar.getInstance()
//        calendar.time = DateUtil.parseDate(day)
//        val start = calendar.timeInMillis / 1000
//        calendar.add(Calendar.DAY_OF_MONTH, 1)
//        val end = calendar.timeInMillis / 1000

        val ex = Example(SscLottery::class.java)
        ex.createCriteria().andEqualTo(SscLottery.GAME, Keys.Vr15.key)
                .andLike(SscLottery.ISSUE_NO, "%" + day + "%")
//                .andGreaterThanOrEqualTo(SscLottery.CREATE_AT, start).andLessThan(SscLottery.CREATE_AT, end)

        ex.orderBy(SscLottery.CREATE_AT).desc()

//        startPage<SscLottery>(page, 50)
        val lottery = dao.selectByExample(ex)
        res = lottery.map { mapOf("issue" to it.issueNo, "number" to it.betNumber) }.toMutableList()
        return Ri.successData(res)
    }

    /**
     *
     */
    open fun export(start: Int = 0, end: Int = 1000): ResponseEntity<Any> {
        var ex = Example(SscLottery::class.java)
        ex.createCriteria().andEqualTo(SscLottery.GAME, Keys.Vr15.key)
        ex.orderBy(SscLottery.CREATE_AT).desc()
        startPage<SscLottery>(start, end)
        val lotterys = dao.selectByExample(ex)
        if (lotterys != null && !lotterys.isEmpty()) {
            var lus = mutableListOf<ExLuData>()
            var sms = mutableListOf<ExSmData>()
            lotterys.forEach(Consumer { item ->
                run {
                    lus.add(exlu(item))
                    sms.add(exsm(item))
                }
            })

            val templateFileName: String = template + "template.xlsx"
            val filename = System.currentTimeMillis().toString() + ".xlsx"
            val tempPath: String = temporary + filename

            val excelWriter: ExcelWriter = EasyExcel.write(tempPath).withTemplate(templateFileName).build()
            val fillConfig = FillConfig.builder().forceNewRow(true).build()

            val writeSheet0 = EasyExcel.writerSheet(0).build()
            excelWriter.fill(lus, fillConfig, writeSheet0)

            val writeSheet1 = EasyExcel.writerSheet(1).build()
            excelWriter.fill(sms, fillConfig, writeSheet1)

            excelWriter.finish()

            val resource = PathResource(Paths.get(tempPath))


            return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename).body(resource)


        }
        return ResponseEntity.status(201).body(null)

    }

    fun exsm(item: SscLottery): ExSmData {
        var sm = ExSmData(item.issueNo!!, item.betNumber!!)
        val nums = item.betNumber!!.map(Character::getNumericValue).toList()

        sm.he = nums.sum()
        if (sm.he <= Keys.Vr15.hebs_divide) {
            sm.hedx = "小"
        } else {
            sm.hedx = "大"
        }
        if (sm.he % 2 == 0) {
            sm.heds = "双"
        } else {
            sm.heds = "单"
        }


        if (nums[0] <= 4) {
            sm.wdx = "小"
        } else {
            sm.wdx = "大"
        }
        if (nums[0] % 2 == 0) {
            sm.wds = "双"
        } else {
            sm.wds = "单"
        }

        if (nums[1] <= 4) {
            sm.qdx = "小"
        } else {
            sm.qdx = "大"
        }
        if (nums[1] % 2 == 0) {
            sm.qds = "双"
        } else {
            sm.qds = "单"
        }

        if (nums[2] <= 4) {
            sm.bdx = "小"
        } else {
            sm.bdx = "大"
        }
        if (nums[2] % 2 == 0) {
            sm.bds = "双"
        } else {
            sm.bds = "单"
        }

        if (nums[3] <= 4) {
            sm.sdx = "小"
        } else {
            sm.sdx = "大"
        }
        if (nums[3] % 2 == 0) {
            sm.sds = "双"
        } else {
            sm.sds = "单"
        }

        if (nums[4] <= 4) {
            sm.gdx = "小"
        } else {
            sm.gdx = "大"
        }
        if (nums[4] % 2 == 0) {
            sm.gds = "双"
        } else {
            sm.gds = "单"
        }

        return sm
    }


    fun exlu(item: SscLottery): ExLuData {
        var lu = ExLuData(item.issueNo!!, item.betNumber!!)
        val nums = item.betNumber!!.map(Character::getNumericValue).toList()
        if (nums[0] > nums[1]) {
            lu.wq = "龙"
        } else if (nums[0] < nums[1]) {
            lu.wq = "虎"
        } else {
            lu.wq = "和"
        }
        if (nums[0] > nums[2]) {
            lu.wb = "龙"
        } else if (nums[0] < nums[2]) {
            lu.wb = "虎"
        } else {
            lu.wb = "和"
        }
        if (nums[0] > nums[3]) {
            lu.ws = "龙"
        } else if (nums[0] < nums[3]) {
            lu.ws = "虎"
        } else {
            lu.ws = "和"
        }
        if (nums[0] > nums[4]) {
            lu.wg = "龙"
        } else if (nums[0] < nums[4]) {
            lu.wg = "虎"
        } else {
            lu.wg = "和"
        }
        if (nums[1] > nums[2]) {
            lu.qb = "龙"
        } else if (nums[1] < nums[2]) {
            lu.qb = "虎"
        } else {
            lu.qb = "和"
        }
        if (nums[1] > nums[3]) {
            lu.qs = "龙"
        } else if (nums[1] < nums[3]) {
            lu.qs = "虎"
        } else {
            lu.qs = "和"
        }
        if (nums[1] > nums[4]) {
            lu.qg = "龙"
        } else if (nums[1] < nums[4]) {
            lu.qg = "虎"
        } else {
            lu.qg = "和"
        }
        if (nums[2] > nums[3]) {
            lu.bs = "龙"
        } else if (nums[2] < nums[3]) {
            lu.bs = "虎"
        } else {
            lu.bs = "和"
        }
        if (nums[2] > nums[4]) {
            lu.bg = "龙"
        } else if (nums[2] < nums[4]) {
            lu.bg = "虎"
        } else {
            lu.bg = "和"
        }
        if (nums[3] > nums[4]) {
            lu.sg = "龙"
        } else if (nums[3] < nums[4]) {
            lu.sg = "虎"
        } else {
            lu.sg = "和"
        }
        return lu
    }


}

