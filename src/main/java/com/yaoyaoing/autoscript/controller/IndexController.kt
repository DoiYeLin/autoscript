package com.yaoyaoing.autoscript.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class IndexController {
    // /actuator/shutdown
    @PostMapping("index")
    fun index() {
    }
}