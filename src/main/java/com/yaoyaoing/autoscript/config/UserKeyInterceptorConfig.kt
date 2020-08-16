package com.yaoyaoing.autoscript.config

import com.yaoyaoing.autoscript.interceptor.UserKeyInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@Order(10)
open class UserKeyInterceptorConfig : WebMvcConfigurer {

    @Bean
    open fun userKeyInterceptor(): UserKeyInterceptor {
        return UserKeyInterceptor()
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        val include = listOf<String>("/out-account/**", "/vr15-lottery/auto/**", "/userkey/**")

        registry.addInterceptor(userKeyInterceptor()).addPathPatterns(include)
    }
}