package com.yaoyaoing.autoscript.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy

@Configuration
@EnableAsync
open class ScheduleConfig {
    // 线程池对拒绝任务（无线程可用）的处理策略，目前只支持AbortPolicy、CallerRunsPolicy；默认为后者
    //调度器shutdown被调用时等待当前被调度的任务完成
    //等待时长
    @get:Bean(name = ["scheduledPoolTaskExecutor"])
    open val asyncThreadPoolTaskExecutor: ThreadPoolTaskExecutor
        get() {
            val taskExecutor = ThreadPoolTaskExecutor()
            taskExecutor.corePoolSize = 5
            taskExecutor.maxPoolSize = 30
            taskExecutor.setQueueCapacity(20)
            taskExecutor.keepAliveSeconds = 60
            taskExecutor.threadNamePrefix = "Ddv-Scheduled-"
            // 线程池对拒绝任务（无线程可用）的处理策略，目前只支持AbortPolicy、CallerRunsPolicy；默认为后者
            taskExecutor.setRejectedExecutionHandler(CallerRunsPolicy())
            //调度器shutdown被调用时等待当前被调度的任务完成
            taskExecutor.setWaitForTasksToCompleteOnShutdown(true)
            //等待时长
            taskExecutor.setAwaitTerminationSeconds(60)
            taskExecutor.initialize()
            return taskExecutor
        }

    companion object {
        private val log = LoggerFactory.getLogger(ScheduleConfig::class.java)
    }
}