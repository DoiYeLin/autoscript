package com.yaoyaoing.autoscript.dto

class Vr15Lottery {

    class Sm {

        lateinit var hbset: MutableList<Path>
        lateinit var hdset: MutableList<Path>

        lateinit var wbset: MutableList<Path>
        lateinit var wdset: MutableList<Path>

        lateinit var qbset: MutableList<Path>
        lateinit var qdset: MutableList<Path>

        lateinit var bbset: MutableList<Path>
        lateinit var bdset: MutableList<Path>

        lateinit var sbset: MutableList<Path>
        lateinit var sdset: MutableList<Path>

        lateinit var gbset: MutableList<Path>
        lateinit var gdset: MutableList<Path>


        /**
         * 双面玩法开奖号码路单
         * @param type
         */
        data class Path(var type: Type, var count: Int = 0)

        enum class Type() {
            BIG,
            SMALL,
            DOUBLE,
            SINGLE
        }
    }

    /**
     * 连续相同
     */
    data class SustainAlike(val type: String, val count: Int = 0) {

    }


}