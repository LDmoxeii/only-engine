package com.only.engine.redis.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.only.engine.redis.RedisInitPrinter
import com.only.engine.redis.aspectj.RepeatSubmitAspect
import com.only.engine.spi.idempotent.TokenProvider
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.EnableAspectJAutoProxy

/**
 * 幂等性自动配置
 *
 * @author LD_moxeii
 */
@AutoConfiguration
@EnableAspectJAutoProxy
class IdempotentAutoConfiguration : RedisInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(IdempotentAutoConfiguration::class.java)
    }

    /**
     * 防重复提交切面
     */
    @Bean
    @ConditionalOnBean(TokenProvider::class)
    fun repeatSubmitAspect(
        tokenProvider: TokenProvider,
        objectMapper: ObjectMapper,
    ): RepeatSubmitAspect {
        printInit(RepeatSubmitAspect::class.java, log)
        return RepeatSubmitAspect(tokenProvider, objectMapper)
    }
}
