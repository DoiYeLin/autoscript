package com.yaoyaoing.autoscript.dto

data class PlanStepDto(var failJump: Int = 0,
                       var failJumpPlan: Int? = 0,
                       var money: Double = 0.0,
                       var winJump: Int = 0)