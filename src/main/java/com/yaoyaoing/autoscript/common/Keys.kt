package com.yaoyaoing.autoscript.common

class Keys {

    companion object {

        const val _true = "true"
        const val _false = "false"

        const val rk_common_score = "common_score"
        const val rk_common_map = "common_map"

        // 从0开始
        const val cacheNumberSize = 50L

        // N期未中暂停一期
        const val betPauseLen = 4

        const val rk_bet_picks = "rk_bet_picks"
    }

    class Vr15 {
        companion object {
            // 开奖数据抓取地址
            const val get_number_url = "https://numbers.videoracing.com/analy.aspx?code=3&analyType=1"

            const val BS = "bs"
            const val DS = "ds"
            val sm_targets = mapOf<String, List<String>>(
                    "bs" to listOf<String>("h_bs", "w_bs", "q_bs", "b_bs", "s_bs", "g_bs"),
                    "ds" to listOf<String>("h_ds", "w_ds", "q_ds", "b_ds", "s_ds", "g_ds")
            )

            const val key = "vr15"

            const val sm = "vr15sm"
            const val lh = "vr15lh"

            // 每天开奖期数
            const val max_issue = 840

            // 早上6点封盘，9点开盘
            const val rk_adjourn = "vr15_adjourn"

            // vr15 是否校准
            const val rk_open_align = "vr15_open_align"


            // redis 缓存, 双面方案玩法缓存
            const val rk_sm_plans = "vr15_sm_plans"

            // vr15 双面 和的大小分界
            const val hebs_divide = 22
            const val otherbs_divide = 4

            // 模拟投注
            const val sim = 0

            // 正式投注
            const val run = 1

            // N期未中暂停一期，缓存上期数据, 只使用一次
            const val rk_vr15_betpause = "rk_vr15_betpause"
        }

        /**
         * 开奖号码单位
         */
        enum class Unit(val code: Int) {
            sum(-1),
            wan(0),
            qian(1),
            bai(2),
            shi(3),
            ge(4)
        }
    }
}