package com.only.engine.satoken.config

import cn.dev33.satoken.filter.SaServletFilter
import cn.dev33.satoken.httpauth.basic.SaHttpBasicUtil
import cn.dev33.satoken.jwt.StpLogicJwtForSimple
import cn.dev33.satoken.stp.StpInterface
import cn.dev33.satoken.stp.StpLogic
import cn.dev33.satoken.util.SaResult
import com.only.engine.SaTokenInitPrinter
import com.only.engine.satoken.core.service.SaPermissionImpl
import com.only.engine.satoken.factory.YmlPropertySourceFactory
import com.only.engine.satoken.handler.SaTokenExceptionHandler
import com.only.engine.satoken.interceptor.SaTokenSecurityInterceptor
import com.only.engine.security.interceptor.SecurityInterceptor
import com.only.engine.security.url.UrlCollector
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.http.HttpStatus

@AutoConfiguration
@ConditionalOnProperty(
    prefix = "only.security",
    name = ["provider"],
    havingValue = "sa-token",
    matchIfMissing = true
)
@PropertySource(value = ["classpath:common-satoken.yml"], factory = YmlPropertySourceFactory::class)
class SaTokenAutoConfiguration(
    @Value("\${spring.boot.admin.client.username:admin}")
    private val actuatorUsername: String,
    @Value("\${spring.boot.admin.client.password:123456}")
    private val actuatorPassword: String,
    private val urlCollector: UrlCollector,
) : SaTokenInitPrinter {

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
    fun stpInterface(): StpInterface {
        val impl = SaPermissionImpl()
        printInit(StpInterface::class.java, log)
        return impl
    }

    @Bean
    @ConditionalOnMissingBean
    fun saTokenExceptionHandler(): SaTokenExceptionHandler {
        val handler = SaTokenExceptionHandler()
        printInit(SaTokenExceptionHandler::class.java, log)
        return handler
    }

    @Bean
    @ConditionalOnMissingBean
    fun saTokenSecurityInterceptor(): SecurityInterceptor {
        val interceptor = SaTokenSecurityInterceptor(urlCollector)
        printInit(SaTokenSecurityInterceptor::class.java, log)
        return interceptor
    }

    @Bean
    @ConditionalOnProperty(name = ["management.endpoints.web.exposure.include"], havingValue = "health")
    fun saServletFilter(): SaServletFilter {
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
}
