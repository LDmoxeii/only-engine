package com.only.engine.security.config

import com.only.engine.security.SecurityInitPrinter
import com.only.engine.security.SecurityManager
import com.only.engine.security.factory.SecurityProviderFactory
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@EnableConfigurationProperties(SecurityProperties::class)
@ComponentScan("com.only.engine.security.url")
@ConditionalOnProperty(prefix = "only.security", name = ["enable"], havingValue = "true", matchIfMissing = true)
class SecurityAutoConfiguration(
    private val securityProperties: SecurityProperties,
    private val securityProviderFactory: SecurityProviderFactory,
) : WebMvcConfigurer, SecurityInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(SecurityAutoConfiguration::class.java)
    }

    @Bean
    fun securityManager(): SecurityManager {
        val manager = SecurityManager(securityProperties, securityProviderFactory)
        printInit(SecurityManager::class.java, log)
        return manager
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        val manager = SecurityManager.getInstance()
        manager.securityInterceptors.forEach { interceptor ->
            registry.addInterceptor(interceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(*securityProperties.excludes)
                .order(interceptor.getOrder())
        }
    }
}
