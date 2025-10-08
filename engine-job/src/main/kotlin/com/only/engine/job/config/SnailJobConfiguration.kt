package com.only.engine.job.config

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import com.aizuda.snailjob.client.common.appender.SnailLogbackAppender
import com.aizuda.snailjob.client.common.event.SnailClientStartingEvent
import com.aizuda.snailjob.client.starter.EnableSnailJob
import com.only.engine.job.JobInitPrinter
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * SnailJob 定时任务自动配置
 *
 * 当满足以下条件时生效：
 * 1. only.job.enabled 属性为 true（默认为 true）
 *
 * 集成了以下功能：
 * - 启用 Spring 调度支持
 * - 启用 SnailJob 客户端
 * - 配置 SnailJob Logback Appender
 *
 * @author LD_moxeii
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "only.engine.job", name = ["enable"], havingValue = "true")
@EnableScheduling
@EnableSnailJob
class SnailJobConfiguration : JobInitPrinter {

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(SnailJobConfiguration::class.java)
    }

    /**
     * 监听 SnailJob 客户端启动事件，配置 Logback Appender
     */
    @EventListener(SnailClientStartingEvent::class)
    fun onStarting(event: SnailClientStartingEvent) {
        printInit(SnailJobConfiguration::class.java, log)
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        val snailAppender = SnailLogbackAppender<ILoggingEvent>().apply {
            name = "snail_log_appender"
            start()
        }
        val rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)
        rootLogger.addAppender(snailAppender)
    }
}
