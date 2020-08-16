package com.yaoyaoing.autoscript.interceptor

import com.alibaba.fastjson.JSON
import com.yaoyaoing.autoscript.common.AuthCurrent
import com.yaoyaoing.autoscript.common.CodeEnum
import com.yaoyaoing.autoscript.common.RedisService
import com.yaoyaoing.autoscript.common.Ri
import com.yaoyaoing.autoscript.entitys.User
import com.yaoyaoing.autoscript.utils.CookieUtil
import com.yaoyaoing.autoscript.utils.SecureUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import java.io.IOException
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SuperviseInterceptor : HandlerInterceptorAdapter() {

    companion object {
        private val log = LoggerFactory.getLogger(SuperviseInterceptor::class.java)
    }

    @Autowired
    private lateinit var redisService: RedisService

    @Throws(Exception::class)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        /**
         * 权限校验： 1、校验是否登陆
         */
        val uri = request.requestURI
        log.info(uri)

        val cookie: Cookie = CookieUtil.getCookieByName(request, "token")
        if (cookie == null) {
            response(response, CodeEnum.NO_LOGIN, "Token丢失，重新登陆", null)
            return false
        }
        var token = cookie.value
        if (token.isNullOrEmpty()) {
            response(response, CodeEnum.NO_LOGIN, "Token丢失，重新登陆")
            return false
        }
        token = SecureUtils.decryptStr(token);
        if (!redisService.hHasKey(User.redisKey, token)) {
            response(response, CodeEnum.NO_LOGIN, "Token丢失，重新登陆")
            return false
        }
        AuthCurrent.supUserId(token)
        return super.preHandle(request, response, handler)
    }

    @Throws(Exception::class)
    override fun postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any, modelAndView: ModelAndView?) {
        super.postHandle(request, response, handler, modelAndView)
    }

    @Throws(IOException::class)
    private fun response(response: HttpServletResponse, codeEnum: CodeEnum, msg: String, data: Any? = null) {
        response.status = HttpServletResponse.SC_OK
        response.setHeader("Content-Type", "application/json")
        val out = response.writer
        out.write(JSON.toJSONString(Ri.create(codeEnum.code, if (StringUtils.isEmpty(msg)) codeEnum.msg else msg,
                data)))
        out.flush()
    }
}
