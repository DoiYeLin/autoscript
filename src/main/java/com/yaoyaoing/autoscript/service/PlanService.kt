package com.yaoyaoing.autoscript.service

import com.yaoyaoing.autoscript.common.Keys
import com.yaoyaoing.autoscript.common.Ri
import com.yaoyaoing.autoscript.dao.BetPlanDao
import com.yaoyaoing.autoscript.dto.PlanDto
import com.yaoyaoing.autoscript.entitys.BetPlan
import org.mybatis.logging.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tk.mybatis.mapper.entity.Example
import java.util.function.Consumer

@Service
class PlanService {

    companion object {
        private val log = LoggerFactory.getLogger(PlanService::class.java)
    }

    @Autowired
    private lateinit var dao: BetPlanDao

    @Autowired
    private lateinit var vr15smPlanService: Vr15SmPlanService

    fun init() {
        val plans = dao.selectAll()
        val gbs = plans.groupBy { it.playMode }.toList()
        gbs.forEach(Consumer {
            when (it.first) {
                Keys.Vr15.sm -> vr15smPlanService.init(it.second)
                Keys.Vr15.lh -> print("")
            }
        })
    }

    fun vr15List(gm: String): Ri<Any> {
        val ex = Example(BetPlan::class.java)
        ex.createCriteria().andEqualTo(BetPlan.GAME, Keys.Vr15.key).andEqualTo(BetPlan.PLAY_MODE, gm)
        val plans = dao.selectByExample(ex)
        val res = mutableListOf<PlanDto>()
        if (!plans.isNullOrEmpty()) {
            plans.forEach(Consumer {
                res.add(PlanDto.convert(it))
            })
        }
        return Ri.successData(res)
    }
}