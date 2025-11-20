package com.only.engine.sms.config

import com.only.engine.sms.SmsInitPrinter
import com.only.engine.sms.core.dao.OnlySmsDao
import org.dromara.sms4j.api.dao.SmsDao
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

/**
 * 短信发送自动配置
 *
 */
@AutoConfiguration(after = [RedisAutoConfiguration::class])
class SmsAutoConfiguration : SmsInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(SmsAutoConfiguration::class.java)
    }

    /**
     * 自定义 SmsDao，基于 RedisUtils 实现短信缓存（重试、拦截等）
     */
    @Bean
    @Primary
    fun smsDao(): SmsDao {
        printInit(OnlySmsDao::class.java, log)
        return OnlySmsDao()
    }

}

