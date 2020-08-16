package com.yaoyaoing.autoscript

import cn.hutool.core.util.IdUtil
import com.yaoyaoing.autoscript.common.CodeEnum
import com.yaoyaoing.autoscript.entitys.UserKeys
import com.yaoyaoing.autoscript.service.UserKeysService
import com.yaoyaoing.autoscript.utils.MyUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [AutoScriptApplication::class])
@Transactional
open class UserKeysTest {

    @Autowired
    private lateinit var service: UserKeysService

    @Test
    @Rollback(false)
    fun add() {
        var user = UserKeys.defaultInstance()
        user.id = IdUtil.objectId()
        user.key = IdUtil.objectId()
        user.desc = "测试"
        user.deviceNum = 1000
        user.createAt = MyUtils.secMillis()

        val res = service.add(user)
        println("添加 userKey :$res")
    }

}