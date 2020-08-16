package com.yaoyaoing.autoscript.service

import com.yaoyaoing.autoscript.common.RedisService
import com.yaoyaoing.autoscript.dao.BetRecordDao
import com.yaoyaoing.autoscript.entitys.BetRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class BetRecordService {

    companion object {
        private val log = LoggerFactory.getLogger(BetRecordService::class.java)
    }

    @Autowired
    private lateinit var dao: BetRecordDao

    @Autowired
    private lateinit var redisService: RedisService


    fun insertSelective(br: BetRecord): Int {
        return dao.insertSelective(br)
    }

    fun updateByPrimaryKeySelective(br: BetRecord): Int {
        return dao.updateByPrimaryKeySelective(br)
    }

    fun openBet() {

    }
}

