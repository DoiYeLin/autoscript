package com.yaoyaoing.autoscript.controller

import com.yaoyaoing.autoscript.common.Ri
import com.yaoyaoing.autoscript.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("user")
class UserController {

    @Autowired
    private lateinit var service: UserService

    @PostMapping("login")
    fun login(account: String, password: String): Ri<Any> {
        return service.login(account, password)
    }

    @PostMapping("signout")
    fun signout(): Ri<Any> {
        return service.signout()
    }
}