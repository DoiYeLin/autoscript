package com.yaoyaoing.autoscript.interceptor

import com.alibaba.fastjson.JSON
import com.yaoyaoing.autoscript.common.AuthCurrent
import com.yaoyaoing.autoscript.common.CodeEnum
import com.yaoyaoing.autoscript.common.Ri
import com.yaoyaoing.autoscript.service.UserKeysService
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class UserKeyInterceptor : HandlerInterceptorAdapter() {

    @Autowired
    private lateinit var service: UserKeysService

    @Throws(Exception::class)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val userKey = request.getHeader("Authentication")
        if (userKey.isNullOrEmpty()) {
            response(response, CodeEnum.USERKEY_EMPTY)
            return false;
        }
        val platform = request.getHeader("platform")
        println(request)
        AuthCurrent.userKey(userKey)
        AuthCurrent.platform(platform)
        return super.preHandle(request, response, handler)
    }

    @Throws(Exception::class)
    override fun afterCompletion(
        request: HttpServletRequest, response: HttpServletResponse, handler: Any,
        ex: Exception?
    ) {
        super.afterCompletion(request, response, handler, ex)
    }

    @Throws(IOException::class)
    private fun response(response: HttpServletResponse, codeEnum: CodeEnum, msg: String? = null, data: Any? = null) {
        response.status = HttpServletResponse.SC_OK
        response.setHeader("Content-Type", "application/json")
        val out = response.writer
        out.write(
            JSON.toJSONString(
                Ri.create(
                    codeEnum.code, if (StringUtils.isEmpty(msg)) codeEnum.msg else msg,
                    data
                )
            )
        )
        out.flush()
    }
}