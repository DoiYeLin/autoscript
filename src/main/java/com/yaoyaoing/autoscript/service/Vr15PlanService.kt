package com.yaoyaoing.autoscript.service

import com.yaoyaoing.autoscript.dao.BetPlanDao
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
class Vr15PlanService {

    companion object {
        private val log = LoggerFactory.getLogger(Vr15PlanService::class.java)
    }

    @Autowired
    private lateinit var dao: BetPlanDao

    fun lottery(args: MutableList<List<Int>>) {
        
    }

}