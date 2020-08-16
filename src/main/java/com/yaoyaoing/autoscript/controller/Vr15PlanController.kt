package com.yaoyaoing.autoscript.controller

import com.yaoyaoing.autoscript.apiargs.Vr15PlanArgs
import com.yaoyaoing.autoscript.common.Keys
import com.yaoyaoing.autoscript.common.Ri
import com.yaoyaoing.autoscript.service.PlanService
import com.yaoyaoing.autoscript.service.Vr15PlanService
import com.yaoyaoing.autoscript.service.Vr15SmPlanService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("vr15-plan")
class Vr15PlanController {

    companion object {
        private val log = LoggerFactory.getLogger(Vr15PlanController::class.java)
    }

    @Autowired
    private lateinit var planService: PlanService

    @Autowired
    private lateinit var smService: Vr15SmPlanService

    /**
     * 获取方案
     */
    @PostMapping("plks")
    fun plks(gm: String): Ri<Any> {
        return when (gm) {
            "sm" -> smService.getkeys()
            else -> Ri.error()
        }
    }

    @PostMapping("plcks")
    fun plcks(key: Int): Ri<Any> {
        return smService.chidKeys(key)
    }

    @PostMapping
    fun list(gm: String): Ri<Any> {
        return when (gm) {
            "sm" -> planService.vr15List(Keys.Vr15.sm)
            "lh" -> planService.vr15List(Keys.Vr15.lh)
            else -> Ri.error()
        }
    }

    /**
     * 添加或者修改方案
     */
    @PostMapping("doiu")
    fun addOrUpdate(gm: String, @ModelAttribute plan: Vr15PlanArgs): Ri<Any> {
        return when (gm) {
            "sm" -> smService.saveSmPlan(plan)
            else -> Ri.error()
        }
    }

    @PostMapping("del")
    fun del(id: String): Ri<Any> {
        return smService.del(id)
    }

    @PostMapping("sim-action")
    fun simAction(gm: String, picks: String?, id: String, open: Boolean): Ri<Any> {
        return when (gm) {
            "sm" -> smService.simAction(id, picks, open)
            else -> Ri.error()
        }
    }
}