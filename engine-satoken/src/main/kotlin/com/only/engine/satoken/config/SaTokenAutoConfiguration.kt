package com.only.engine.satoken.config

import com.only.engine.SaTokenInitPrinter
import com.only.engine.satoken.adapter.SaTokenUserDetailsProvider
import com.only.engine.satoken.factory.SaTokenSecurityProviderFactory
import com.only.engine.satoken.handler.SaTokenExceptionHandler
import com.only.engine.security.factory.SecurityProviderFactory
import com.only.engine.security.url.UrlCollector
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan("com.only.engine.satoken")
@ConditionalOnProperty(
    prefix = "only.security",
    name = ["provider"],
    havingValue = "sa-token",
    matchIfMissing = true
)
class SaTokenAutoConfiguration : SaTokenInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(SaTokenAutoConfiguration::class.java)
    }

    @Bean
    fun saTokenSecurityProviderFactory(
        userDetailsProvider: SaTokenUserDetailsProvider,
        urlCollector: UrlCollector,
    ): SecurityProviderFactory {
        val factory = SaTokenSecurityProviderFactory(userDetailsProvider, urlCollector)
        printInit(SaTokenSecurityProviderFactory::class.java, log)
        return factory
    }

    /**
     * Sa-Token异常处理器
     */
    @Bean
    @ConditionalOnMissingBean
    fun saTokenExceptionHandler(): SaTokenExceptionHandler {
        val handler = SaTokenExceptionHandler()
        printInit(SaTokenExceptionHandler::class.java, log)
        return handler
    }
}