package com.only.engine.security.config

import com.only.engine.security.SecurityInitPrinter
import com.only.engine.security.config.properties.SecurityProperties
import com.only.engine.spi.interceptor.SecurityInterceptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@AutoConfiguration
@EnableConfigurationProperties(SecurityProperties::class)
@ConditionalOnProperty(prefix = "only.engine.security", name = ["enable"], havingValue = "true")
class SecurityConfiguration(
    private val securityProperties: SecurityProperties,
    private val securityInterceptorsProvider: ObjectProvider<SecurityInterceptor>,
) : WebMvcConfigurer, SecurityInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(SecurityConfiguration::class.java)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        securityInterceptorsProvider.orderedStream().forEach { interceptor ->
            registry.addInterceptor(interceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(*securityProperties.excludes)
            printInit(
                "${interceptor::class.simpleName} registered with exclude paths: ${securityProperties.excludes.joinToString()}",
                log
            )
        }
    }
}
