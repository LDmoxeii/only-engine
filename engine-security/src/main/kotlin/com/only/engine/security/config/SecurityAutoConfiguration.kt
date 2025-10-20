package com.only.engine.security.config

import com.only.engine.security.SecurityInitPrinter
import com.only.engine.security.config.properties.SecurityProperties
import com.only.engine.spi.security.SecurityInterceptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.handler.MappedInterceptor

/**
 * Security 自动配置
 *
 * 使用 MappedInterceptor Bean 注册拦截器，避免循环依赖
 */
@AutoConfiguration
@EnableConfigurationProperties(SecurityProperties::class)
@ConditionalOnProperty(prefix = "only.engine.security", name = ["enable"], havingValue = "true")
class SecurityAutoConfiguration(
    private val securityProperties: SecurityProperties,
) : SecurityInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(SecurityAutoConfiguration::class.java)
    }

    /**
     * 通过注册 MappedInterceptor Bean 的方式添加拦截器
     *
     * 优点：
     * 1. 避免实现 WebMvcConfigurer，不会在 RequestMappingHandlerMapping 初始化时被回调
     * 2. 使用 ObjectProvider 延迟获取 SecurityInterceptor Bean
     * 3. 每个 SecurityInterceptor 会被自动包装成独立的 MappedInterceptor
     */
    @Bean
    fun securityMappedInterceptors(
        securityInterceptorsProvider: ObjectProvider<SecurityInterceptor>,
    ): List<MappedInterceptor> {
        return securityInterceptorsProvider.orderedStream().map { interceptor ->
            printInit(
                "${interceptor::class.simpleName} registered with exclude paths: ${securityProperties.excludes.joinToString()}",
                log
            )

            MappedInterceptor(
                arrayOf("/**"),                    // includePatterns
                securityProperties.excludes,        // excludePatterns
                interceptor                         // interceptor
            )
        }.toList()
    }
}
