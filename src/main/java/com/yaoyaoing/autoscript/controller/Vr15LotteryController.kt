package com.yaoyaoing.autoscript.controller

import com.yaoyaoing.autoscript.common.Ri
import com.yaoyaoing.autoscript.dto.SscBetDto
import com.yaoyaoing.autoscript.service.Vr15LotteryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("vr15-lottery")
class Vr15LotteryController {

    @Autowired
    private lateinit var service: Vr15LotteryService

//    @RequestMapping(value = ["auto/bet-number"], method = [RequestMethod.GET, RequestMethod.POST])
//    fun betNumber(ssc: SscBetDto): Ri<Any>? {
//        return service.openNumber(ssc)
//    }

    /**
     * 查看近30期开奖结果
     */
    @RequestMapping("b/trend")
    fun cache(day: String, page: Int): Ri<Any> {
        return service.cache(day, page)
    }

    @PostMapping("b/align")
    fun align(): Ri<Any> {
        return service.vr15_open_align()
    }

    @PostMapping("b/export")
    fun export(start: Int, limit: Int): ResponseEntity<Any> {
        return service.export(start, limit)
    }
}