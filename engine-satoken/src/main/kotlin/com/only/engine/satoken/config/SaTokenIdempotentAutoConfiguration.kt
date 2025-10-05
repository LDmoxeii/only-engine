package com.only.engine.satoken.config

import com.only.engine.satoken.idempotent.SaTokenProvider
import com.only.engine.spi.provider.TokenProvider
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Sa-Token 幂等性自动配置
 *
 * 提供基于 Sa-Token 的 TokenProvider 实现
 *
 * @author LD_moxeii
 */
@AutoConfiguration
class SaTokenIdempotentAutoConfiguration {

    companion object {
        private val log = LoggerFactory.getLogger(SaTokenIdempotentAutoConfiguration::class.java)
    }

    @Bean
    @ConditionalOnMissingBean(TokenProvider::class)
    fun saTokenProvider(): TokenProvider {
        log.info("✓ 初始化 SaTokenProvider")
        return SaTokenProvider()
    }
}
