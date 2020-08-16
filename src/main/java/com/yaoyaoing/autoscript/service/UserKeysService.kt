package com.yaoyaoing.autoscript.service

import com.alibaba.fastjson.JSON
import com.yaoyaoing.autoscript.common.RedisService
import com.yaoyaoing.autoscript.dao.UserKeysDao
import com.yaoyaoing.autoscript.entitys.UserKeys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
class UserKeysService {
    @Autowired
    private lateinit var dao: UserKeysDao

    @Autowired
    private lateinit var redisService: RedisService

    fun add(userKeys: UserKeys): Boolean {
        val ins = dao.insertSelective(userKeys)
        if (ins > 0) {
            return true
        }
        return false
    }

    fun cache() {
        val userKeys = dao.selectAll()
        for (item in userKeys) {
            redisService.hSet(UserKeys.user_key_cache, item!!.key!!, item)
        }
    }

    fun has(key: String): Boolean {
        return redisService.hHasKey(UserKeys.user_key_cache, key)
    }
}