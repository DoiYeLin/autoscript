package com.yaoyaoing.autoscript.config

import com.yaoyaoing.autoscript.common.CodeEnum
import com.yaoyaoing.autoscript.common.Ri
import com.yaoyaoing.autoscript.common.Ri.Companion.create
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }


    @ExceptionHandler(Exception::class)
    fun exceptionHandler(exception: Exception): Ri<*> {
        log.error("global_exception ", exception);
        exception.printStackTrace()
        return create<Any>(CodeEnum.SERVER_ERROR)
    }
}