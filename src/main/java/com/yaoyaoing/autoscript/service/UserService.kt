package com.yaoyaoing.autoscript.service

import cn.hutool.crypto.SecureUtil
import com.alibaba.fastjson.JSON
import com.yaoyaoing.autoscript.common.AuthCurrent
import com.yaoyaoing.autoscript.common.RedisService
import com.yaoyaoing.autoscript.common.Ri
import com.yaoyaoing.autoscript.dao.UserDao
import com.yaoyaoing.autoscript.entitys.User
import com.yaoyaoing.autoscript.utils.CookieUtil
import com.yaoyaoing.autoscript.utils.SecureUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tk.mybatis.mapper.entity.Example
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Service
class UserService {

    @Autowired
    private lateinit var dao: UserDao

    @Autowired
    private lateinit var redisService: RedisService

    @Autowired
    private lateinit var request: HttpServletRequest

    @Autowired
    private lateinit var response: HttpServletResponse


    fun login(account: String, password: String): Ri<Any> {
        if (account.isNullOrEmpty() || password.isNullOrEmpty()) {
            return Ri.error()
        }
        var ex = Example(User::class.java)
        ex.createCriteria().andEqualTo(User.ACCOUNT, account)
        val user = dao.selectOneByExample(ex)
        if (user != null) {
            if (SecureUtil.md5(password).equals(user.password)) {
                redisService.hSet(User.redisKey, user.id!!, JSON.toJSONString(user))
                val token = SecureUtils.encryptBase64(user.id)
                CookieUtil.addCookie(response, "token", token)
                return Ri.successData(mapOf("token" to token))
            }
        }
        return Ri.error()
    }


    fun signout(): Ri<Any> {
        redisService.hDel(User.redisKey, AuthCurrent.supUserId())
        return Ri.success()
    }
}