package com.yaoyaoing.autoscript.task

import com.yaoyaoing.autoscript.common.Keys
import com.yaoyaoing.autoscript.common.RedisService
import com.yaoyaoing.autoscript.service.Vr15LotteryService
import com.yaoyaoing.autoscript.utils.MyUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Schedule {

    companion object {
        private val log = LoggerFactory.getLogger(Schedule::class.java)
        private var vr15LastGet: Long? = null
    }


    @Autowired
    private lateinit var vr15LotteryService: Vr15LotteryService

    @Autowired
    private lateinit var redisService: RedisService

    @Scheduled(fixedRate = 1000, initialDelay = 10000)
    fun vr15Number() {
        if (!redisService.hasKey(Keys.Vr15.rk_adjourn)) {
//            val align = redisService.zscore(Keys.rk_common_score, Keys.Vr15.rk_open_align)
//            if (align.compareTo(0) == 0) {
            if (vr15LastGet == null) {
                vr15LastGet = MyUtils.secMillis() + 10
                vr15LotteryService.getNumbers(0, 0)
                println("同步开奖号码")
            } else {
                val cur = MyUtils.secMillis()
                if (cur >= vr15LastGet!!) {
                    vr15LastGet = cur + 10
                    vr15LotteryService.getNumbers(0, 0)
                    println("同步开奖号码")
                }
            }
//            } else {
//                val next = redisService.zscoreAdd(Keys.rk_common_score, Keys.Vr15.rk_open_align, (-1).toDouble())
//                if (next > 0 && next < 2) {
//                    redisService.zadd(Keys.rk_common_score, Keys.Vr15.rk_open_align, 88.0)
//                    vr15LotteryService.getNumbers(1, 0)
//                    println("同步开奖号码")
//                }
//            }
        }
    }
}