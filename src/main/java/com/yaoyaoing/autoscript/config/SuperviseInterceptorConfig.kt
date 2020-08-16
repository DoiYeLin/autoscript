package com.yaoyaoing.autoscript.config

import com.yaoyaoing.autoscript.interceptor.SuperviseInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@Order(10)
open class SuperviseInterceptorConfig : WebMvcConfigurer {

    @Bean
    open fun superviseInterceptor(): SuperviseInterceptor {
        return SuperviseInterceptor()
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        val include = listOf<String>("/vr15-lottery/b/**")

        registry.addInterceptor(superviseInterceptor()).addPathPatterns(include)
    }
}