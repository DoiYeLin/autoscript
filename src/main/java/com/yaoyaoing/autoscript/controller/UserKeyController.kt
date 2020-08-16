package com.yaoyaoing.autoscript.controller

import com.yaoyaoing.autoscript.service.UserKeysService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("userkey")
class UserKeyController {

    private lateinit var userKeysService: UserKeysService

    

}