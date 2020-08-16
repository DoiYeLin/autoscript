package com.yaoyaoing.autoscript.controller

import com.yaoyaoing.autoscript.common.Ri
import com.yaoyaoing.autoscript.service.OutAccountService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("out-account")
class OutAccountController {

    @Autowired
    private lateinit var service: OutAccountService

    @RequestMapping(value = ["get"], method = [RequestMethod.GET, RequestMethod.POST])
    fun get(): Ri<Any>? {
        return service.getAccount()
    }

}