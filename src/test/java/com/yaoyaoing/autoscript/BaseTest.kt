package com.yaoyaoing.autoscript

import cn.hutool.core.date.DateUtil
import cn.hutool.core.util.IdUtil
import cn.hutool.core.util.RandomUtil
import cn.hutool.crypto.SecureUtil
import cn.hutool.crypto.digest.MD5
import com.google.gson.Gson
import com.yaoyaoing.autoscript.common.Keys
import com.yaoyaoing.autoscript.dto.PlanDto
import com.yaoyaoing.autoscript.dto.Vr15Lottery
import com.yaoyaoing.autoscript.service.Vr15SmPlanZeroService
import com.yaoyaoing.autoscript.utils.MyUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import java.io.File
import javax.management.ListenerNotFoundException
import kotlin.jvm.internal.Ref
import kotlin.math.exp
import kotlin.math.ln

object BaseTest {

    data class inex(val issue: String, val income: Double, val expend: Int, val ext: String? = null)

    @JvmStatic
    fun main(args: Array<String>) {
//        val datas = listOf<inex>(
//                inex("20200715", (14 + 13 + 16 + 13 + 9 + 6 + 17 + 11 + 10 + 22 + 13 + 12 + 13 + 13 + 9 + 16 + 14 + 13 + 15 + 16 + 9 + 9 + 16 + 16) * 9.6, (350 * 4)),
//                inex("20200716", (16 + 14 + 18 + 13 + 13 + 10 + 11 + 8 + 14 + 12 + 12 + 10 + 6 + 17 + 7 + 14 + 14 + 11 + 8 + 14 + 6 + 6 + 12 + 13) * 9.6, (350 * 2), "data:781"),
//                inex("20200717", (8 + 11 + 10 + 15 + 11 + 16 + 11 + 19 + 11 + 10 + 10 + 10 + 18 + 18 + 17 + 16 + 12 + 11 + 17 + 11 + 12 + 9 + 11 + 11) * 9.6, (350 * 2), "data:811"),
//                inex("20200718", (12 + 13 + 15 + 10 + 13 + 11 + 15 + 14 + 10 + 11 + 17 + 9 + 11 + 11 + 7 + 10 + 8 + 13 + 11 + 16 + 15 + 18 + 13 + 12) * 9.6, (350 * 5), "data:838"),
//                inex("20200719", (15 + 15 + 12 + 13 + 12 + 9 + 11 + 12 + 16 + 10 + 13 + 13 + 13 + 13 + 17 + 14 + 16 + 14 + 12 + 12 + 11 + 14 + 13 + 12) * 9.6, (350 * 6)),
//                inex("20200720", (8 + 10 + 8 + 6 + 5 + 13 + 10 + 12 + 11 + 11 + 8 + 5 + 8 + 9 + 8 + 7 + 4 + 7 + 6 + 9 + 12 + 7 + 9 + 7) * 9.6, (350 * 6), "data:563"),
//                inex("20200721", (19 + 12 + 12 + 9 + 17 + 13 + 14 + 20 + 14 + 14 + 11 + 8 + 13 + 12 + 10 + 10 + 5 + 14 + 17 + 7 + 10 + 15 + 12 + 15) * 9.6, (350 * 3)),
//                inex("20200722", (8 + 11 + 12 + 7 + 14 + 14 + 11 + 9 + 15 + 14 + 14 + 14 + 9 + 13 + 13 + 14 + 13 + 12 + 11 + 15 + 12 + 10 + 5 + 14) * 9.6, (350 * 4)),
//                inex("20200723", (11 + 12 + 14 + 6 + 16 + 13 + 10 + 10 + 16 + 10 + 20 + 9 + 15 + 8 + 5 + 15 + 19 + 12 + 14 + 18 + 9 + 9 + 7 + 18) * 9.6, (350 * 6)),
//                inex("20200724", (12 + 14 + 12 + 10 + 10 + 12 + 15 + 9 + 11 + 9 + 12 + 9 + 17 + 13 + 12 + 11 + 12 + 16 + 8 + 16 + 10 + 13 + 14 + 7) * 9.6, (350 * 6)),
//                inex("20200725", (10 + 15 + 9 + 18 + 12 + 7 + 13 + 14 + 9 + 13 + 9 + 16 + 12 + 17 + 13 + 12 + 14 + 17 + 14 + 10 + 12 + 13 + 10 + 21) * 9.6, (350 * 3)),
//                inex("20200726", (21 + 15 + 11 + 12 + 14 + 8 + 12 + 17 + 12 + 13 + 16 + 11 + 9 + 8 + 13 + 9 + 11 + 15 + 13 + 14 + 10 + 17 + 10 + 6) * 9.6, (350 * 6)),
//                inex("20200730", (8 + 10 + 17 + 8 + 5 + 8 + 11 + 12 + 9 + 15 + 12 + 11 + 11 + 12 + 7 + 13 + 8 + 10 + 12 + 5 + 9 + 11 + 10 + 7) * 9.6, (350 * 4), "data:650"),
//                inex("20200731", (13 + 8 + 11 + 11 + 10 + 5 + 7 + 5 + 8 + 6 + 8 + 15 + 7 + 8 + 7 + 6 + 13 + 7 + 8 + 8 + 8 + 10 + 7 + 13) * 9.6, (350 * 6), "data:535"),
//                inex("20200801", (11 + 11 + 12 + 10 + 16 + 14 + 8 + 7 + 13 + 10 + 14 + 10 + 10 + 10 + 13 + 20 + 10 + 10 + 19 + 12 + 8 + 20 + 15 + 9) * 9.6, (350 * 0), "data:793"),
//                inex("20200802", (14 + 8 + 14 + 18 + 11 + 7 + 16 + 9 + 4 + 8 + 9 + 9 + 11 + 8 + 12 + 13 + 9 + 16 + 14 + 12 + 9 + 7 + 10 + 6) * 9.6, (350 * 4), "data:721"),
//                inex("20200803", (11 + 13 + 12 + 6 + 13 + 6 + 13 + 9 + 10 + 6 + 11 + 10 + 8 + 11 + 6 + 19 + 9 + 15 + 12 + 16 + 7 + 13 + 10 + 8) * 9.6, (350 * 2)),
//                inex("20200804", (14 + 11 + 19 + 14 + 11 + 19 + 11 + 14 + 7 + 15 + 6 + 10 + 10 + 14 + 13 + 7 + 11 + 16 + 15 + 10 + 18 + 11 + 15 + 10) * 9.6, (350 * 7)),
//                inex("20200805", (13 + 9 + 16 + 9 + 16 + 14 + 11 + 16 + 8 + 9 + 13 + 18 + 12 + 17 + 13 + 13 + 16 + 10 + 17 + 10 + 14 + 14 + 13 + 13) * 9.6, (350 * 5)),
//                inex("20200806", (10 + 9 + 12 + 10 + 6 + 12 + 9 + 10 + 15 + 9 + 10 + 7 + 10 + 9 + 12 + 14 + 10 + 4 + 14 + 9 + 9 + 7 + 5 + 13) * 9.6, (350 * 1)),
//                inex("20200807", (10 + 12 + 13 + 8 + 11 + 12 + 11 + 15 + 9 + 13 + 11 + 11 + 12 + 17 + 10 + 16 + 13 + 14 + 10 + 9 + 8 + 14 + 15 + 10) * 9.6, (350 * 1)),
//                inex("20200808", (12 + 14 + 12 + 11 + 14 + 12 + 10 + 15 + 16 + 17 + 17 + 6 + 16 + 19 + 20 + 4 + 12 + 11 + 19 + 13 + 13 + 14 + 13 + 18) * 9.6, (350 * 12)),
//                inex("wait..", 0.0, 1000)
//        )
//
//        for ((inx, data) in datas.withIndex()) {
//            if (inx < datas.size - 1) {
//                print("${inx + 1}.  ${data.issue}  ->  ${MyUtils.keep2Decimal(data.income)} - ${data.expend} = ${MyUtils.keep2Decimal(data.income - data.expend)}")
//                if (data.ext != null) {
//                    println("                    ${data.ext}")
//                } else {
//                    println("")
//                }
//            }
//        }

        for (i in 0 until 30) {
//            println("m" + RandomUtil.randomString(8))
        }

        println(3 / 2)
    }
}