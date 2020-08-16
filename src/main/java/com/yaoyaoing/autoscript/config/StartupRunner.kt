package com.yaoyaoing.autoscript.config

import com.yaoyaoing.autoscript.common.Keys
import com.yaoyaoing.autoscript.common.RedisService
import com.yaoyaoing.autoscript.entitys.OutAccount
import com.yaoyaoing.autoscript.service.OutAccountService
import com.yaoyaoing.autoscript.service.PlanService
import com.yaoyaoing.autoscript.service.UserKeysService
import com.yaoyaoing.autoscript.service.Vr15LotteryService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(value = 1)
class StartupRunner : CommandLineRunner {

    @Autowired
    lateinit var userKeysService: UserKeysService

    @Autowired
    lateinit var planService: PlanService

    @Autowired
    lateinit var redisService: RedisService

    @Autowired
    lateinit var outAccountService: OutAccountService

    @Autowired
    lateinit var vr15LotteryService: Vr15LotteryService

    companion object {
        private val log = LoggerFactory.getLogger(StartupRunner::class.java)
    }

    @Throws(Exception::class)
    override fun run(vararg args: String) {
        redisService.zrem(Keys.rk_common_score, Keys.Vr15.rk_open_align)
        userKeysService.cache()
        planService.init()
        outAccountService.deliverAllAcc()
        vr15LotteryService.getNumbers(0, 0)
    }
}