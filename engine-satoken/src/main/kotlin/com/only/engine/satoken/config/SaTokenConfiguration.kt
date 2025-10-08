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
import com.only.engine.satoken.core.service.SaPermission
import com.only.engine.satoken.handler.SaTokenExceptionHandler
import com.only.engine.satoken.idempotent.SaTokenProvider
import com.only.engine.satoken.interceptor.SaTokenSecurityInterceptor
import com.only.engine.spi.interceptor.SecurityInterceptor
import com.only.engine.spi.provider.TokenProvider
import com.only.engine.spi.service.PermissionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.http.HttpStatus

@AutoConfiguration
@ConditionalOnProperty(prefix = "only.engine.security", name = ["provider"], havingValue = "sa-token")
@PropertySource(value = ["classpath:common-satoken.yml"], factory = YmlPropertySourceFactory::class)
class SaTokenConfiguration() : SaTokenInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(SaTokenConfiguration::class.java)
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
    fun saTokenExceptionHandler(): SaTokenExceptionHandler {
        val handler = SaTokenExceptionHandler()
        printInit(SaTokenExceptionHandler::class.java, log)
        return handler
    }

    @Bean
    @ConditionalOnMissingBean
    fun saTokenSecurityInterceptor(urlCollector: UrlCollector): SecurityInterceptor {
        val interceptor = SaTokenSecurityInterceptor(urlCollector)
        printInit(SaTokenSecurityInterceptor::class.java, log)
        return interceptor
    }

    @Bean
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
    @ConditionalOnMissingBean(TokenProvider::class)
    fun saTokenProvider(): TokenProvider {
        printInit(SaTokenProvider::class.java, log)

        return SaTokenProvider()
    }
}
