package com.only.engine.satoken.config

import cn.dev33.satoken.filter.SaServletFilter
import cn.dev33.satoken.httpauth.basic.SaHttpBasicUtil
import cn.dev33.satoken.jwt.StpLogicJwtForSimple
import cn.dev33.satoken.stp.StpInterface
import cn.dev33.satoken.stp.StpLogic
import cn.dev33.satoken.util.SaResult
import cn.hutool.extra.spring.SpringUtil
import com.only.engine.collector.UrlCollector
import com.only.engine.factory.YmlPropertySourceFactory
import com.only.engine.satoken.SaTokenInitPrinter
import com.only.engine.satoken.adice.SaTokenExceptionHandlerAdvice
import com.only.engine.satoken.config.properties.SaTokenProperties
import com.only.engine.satoken.core.service.SaPermission
import com.only.engine.satoken.interceptor.SaTokenSecurityInterceptor
import com.only.engine.satoken.provider.SaTokenProvider
import com.only.engine.spi.authentication.PermissionService
import com.only.engine.spi.idempotent.TokenProvider
import com.only.engine.spi.security.SecurityInterceptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.http.HttpStatus

@AutoConfiguration
@EnableConfigurationProperties(SaTokenProperties::class)
@ConditionalOnProperty(prefix = "only.engine.sa-token", name = ["enable"], havingValue = "true")
@PropertySource(value = ["classpath:common-sa-token.yml"], factory = YmlPropertySourceFactory::class)
class SaTokenAutoConfiguration() : SaTokenInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(SaTokenAutoConfiguration::class.java)
    }

    @Bean
    @ConditionalOnMissingBean
    fun stpLogicJwt(): StpLogic {
        val logic = StpLogicJwtForSimple()
        printInit(StpLogic::class.java, log)
        return logic
    }

    @Bean
    @ConditionalOnMissingBean
    fun stpInterface(permissionService: ObjectProvider<PermissionService>): StpInterface {
        val impl = SaPermission(permissionService)
        printInit(StpInterface::class.java, log)
        return impl
    }

    @Bean
    @ConditionalOnMissingBean
    fun saTokenExceptionHandler(): SaTokenExceptionHandlerAdvice {
        val handler = SaTokenExceptionHandlerAdvice()
        printInit(SaTokenExceptionHandlerAdvice::class.java, log)
        return handler
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "only.engine.security.provider",
        name = ["security-interceptor"],
        havingValue = "sa-token"
    )
    fun saTokenSecurityInterceptor(urlCollector: UrlCollector): SecurityInterceptor {
        val interceptor = SaTokenSecurityInterceptor(urlCollector)
        printInit(SaTokenSecurityInterceptor::class.java, log)
        return interceptor
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = ["management.endpoints.web.exposure.include"], havingValue = "health")
    fun saServletFilter(): SaServletFilter {
        val actuatorUsername = SpringUtil.getProperty("spring.boot.admin.client.username")
        val actuatorPassword = SpringUtil.getProperty("spring.boot.admin.client.password")

        val filter = SaServletFilter()
            .addInclude("/actuator", "/actuator/**")
            .setAuth {
                SaHttpBasicUtil.check("$actuatorUsername:$actuatorPassword")
            }
            .setError { e ->
                SaResult.error(e.message).setCode(HttpStatus.UNAUTHORIZED.value())
            }

        printInit(SaServletFilter::class.java, log)
        return filter
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "only.engine.redis.provider", name = ["token-provider"], havingValue = "redis")
    fun saTokenProvider(): TokenProvider {
        printInit(SaTokenProvider::class.java, log)

        return SaTokenProvider()
    }
}
