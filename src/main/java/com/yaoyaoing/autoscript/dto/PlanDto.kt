package com.yaoyaoing.autoscript.dto

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yaoyaoing.autoscript.entitys.BetPlan

data class PlanDto(
        val id: String,
        val game: String,
        val playMode: String,
        val key: Int,
        val name: String,
        val accNum: Int,
        var simulate: Boolean? = false,
        var runing: Boolean? = false,
        var pickMode: String? = null,
        var odds: Double? = 0.0
) {
    var step: List<PlanStepDto>? = null

    companion object {
        fun convert(arg: BetPlan): PlanDto {
            var dto = PlanDto(
                    arg.id!!,
                    arg.game!!,
                    arg.playMode!!,
                    arg.key!!,
                    arg.name!!,
                    arg.accNum!!,
                    arg.simulate!!,
                    arg.runing!!,
                    arg.pickMode,
                    arg.odds
            )
            val type = object : TypeToken<List<PlanStepDto>>() {}.type
            val steps = Gson().fromJson<List<PlanStepDto>>(arg.value, type)
            dto.step = steps
            return dto
        }
    }
}