package com.only.engine.redis.config

import com.only.engine.redis.RedisInitPrinter
import com.only.engine.redis.store.RedisCaptchaStore
import com.only.engine.spi.captcha.CaptchaStore
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean

/**
 * Redis 验证码存储自动配置
 *
 * 当满足以下条件时生效:
 * 1. only.engine.captcha.enable=true (默认为 true)
 * 2. only.engine.captcha.provider.store=redis
 *
 * 提供:
 * - RedisCaptchaStore Bean: 基于 Redis 的验证码存储实现
 *
 * @author LD_moxeii
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "only.engine.redis", name = ["enable"], havingValue = "true")
class RedisCaptchaAutoConfiguration() : RedisInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(RedisCaptchaAutoConfiguration::class.java)
    }

    init {
        printInit(RedisCaptchaAutoConfiguration::class.java, log)
    }

    /**
     * 注册 RedisCaptchaStore Bean
     *
     * 仅在配置 only.engine.captcha.provider.store=redis 时生效
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "only.engine.captcha.provider", name = ["store"], havingValue = "redis")
    fun redisCaptchaStore(): CaptchaStore {
        printInit(RedisCaptchaStore::class.java, log)
        return RedisCaptchaStore()
    }
}
