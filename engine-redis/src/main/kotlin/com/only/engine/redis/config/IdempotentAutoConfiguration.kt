package com.only.engine.redis.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.only.engine.redis.RedisInitPrinter
import com.only.engine.redis.aspectj.RepeatSubmitAspect
import com.only.engine.redis.idempotent.DefaultTokenProvider
import com.only.engine.redis.idempotent.TokenProvider
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.core.annotation.Order

/**
 * 幂等性自动配置
 *
 * @author LD_moxeii
 */
@AutoConfiguration
@ConditionalOnBean(RedissonClient::class)
@EnableAspectJAutoProxy
class IdempotentAutoConfiguration : RedisInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(IdempotentAutoConfiguration::class.java)
    }

    /**
     * 默认 Token 提供者
     *
     * Order = Integer.MAX_VALUE (最低优先级)
     * 任何其他实现都可以覆盖它
     */
    @Bean
    @ConditionalOnMissingBean(TokenProvider::class)
    @ConditionalOnMissingClass("cn.dev33.satoken.SaManager")
    @Order(Integer.MAX_VALUE)
    fun defaultTokenProvider(): TokenProvider {
        printInit(DefaultTokenProvider::class.java, log)
        log.warn("⚠️ 使用默认 TokenProvider (建议引入具体实现，如 engine-satoken)")
        return DefaultTokenProvider()
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
        log.info("✓ 使用 TokenProvider 实现: ${tokenProvider.javaClass.simpleName}")
        return RepeatSubmitAspect(tokenProvider, objectMapper)
    }
}
