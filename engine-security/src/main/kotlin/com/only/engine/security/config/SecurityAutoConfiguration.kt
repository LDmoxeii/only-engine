package com.only.engine.security.config

import com.only.engine.security.SecurityInitPrinter
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@EnableConfigurationProperties(SecurityProperties::class)
@ConditionalOnProperty(prefix = "only.security", name = ["enable"], havingValue = "true", matchIfMissing = true)
class SecurityAutoConfiguration(
    private val securityProperties: SecurityProperties,
    private val interceptors: List<HandlerInterceptor>,
) : WebMvcConfigurer, SecurityInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(SecurityAutoConfiguration::class.java)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        interceptors.forEach { interceptor ->
            registry.addInterceptor(interceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(*securityProperties.excludes)
        }
    }
}
