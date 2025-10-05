package com.only.engine.satoken.config

import com.only.engine.redis.idempotent.TokenProvider
import com.only.engine.satoken.idempotent.SaTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order

/**
 * Sa-Token 幂等性自动配置
 *
 * 提供基于 Sa-Token 的 TokenProvider 实现
 *
 * @author LD_moxeii
 */
@AutoConfiguration
@ConditionalOnClass(name = ["cn.dev33.satoken.SaManager"])
class SaTokenIdempotentAutoConfiguration {

    companion object {
        private val log = LoggerFactory.getLogger(SaTokenIdempotentAutoConfiguration::class.java)
    }

    /**
     * Sa-Token Token 提供者
     *
     * Order = 100 (高于默认实现)
     */
    @Bean
    @ConditionalOnMissingBean(TokenProvider::class)
    @Order(100)
    fun saTokenProvider(): TokenProvider {
        log.info("✓ 初始化 SaTokenProvider")
        return SaTokenProvider()
    }
}
