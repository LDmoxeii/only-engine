package com.only.engine.web.config

import com.only.engine.web.WebInitPrinter
import com.only.engine.web.config.properties.WebProperties
import com.only.engine.web.filter.HealthCheckFilter
import com.only.engine.web.filter.RequestBodyWrapperFilter
import com.only.engine.web.filter.ThreadLocalFilter
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered

@AutoConfiguration
@EnableConfigurationProperties(WebProperties::class)
class WebFilterConfiguration : WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(WebFilterConfiguration::class.java)
    }

    /**
     * 健康检查过滤器
     */
    @Bean(HealthCheckFilter.BEAN_NAME)
    @ConditionalOnMissingBean(name = [HealthCheckFilter.BEAN_NAME])
    @ConditionalOnProperty(prefix = "only.web.filter.health-check", name = ["enable"], matchIfMissing = true)
    fun healthCheckFilter(): FilterRegistrationBean<HealthCheckFilter> {
        return FilterRegistrationBean(HealthCheckFilter()).apply {
            addUrlPatterns("/actuator/health")
            setName(HealthCheckFilter.BEAN_NAME)
            isAsyncSupported = true
            order = Ordered.HIGHEST_PRECEDENCE
        }
    }

    /**
     * 请求体包装过滤器
     */
    @Bean(RequestBodyWrapperFilter.BEAN_NAME)
    @ConditionalOnMissingBean(name = [RequestBodyWrapperFilter.BEAN_NAME])
    @ConditionalOnProperty(prefix = "only.web.filter.request-body", name = ["enable"], matchIfMissing = true)
    fun requestBodyWrapperFilter(webProperties: WebProperties): FilterRegistrationBean<RequestBodyWrapperFilter> {
        val skipPath = webProperties.filter.userLogin.skipPaths.apply {
            addAll(webProperties.filter.requestBody.filterUris)
        }

        val filter = RequestBodyWrapperFilter(skipPath)

        return FilterRegistrationBean(filter).apply {
            addUrlPatterns("/*")
            setName(RequestBodyWrapperFilter.BEAN_NAME)
            isAsyncSupported = true
            order = 1
        }
    }

    /**
     * 线程本地变量过滤器
     */
    @Bean(ThreadLocalFilter.BEAN_NAME)
    @ConditionalOnMissingBean(name = [ThreadLocalFilter.BEAN_NAME])
    @ConditionalOnProperty(prefix = "only.web.filter.thread-local", name = ["enable"], matchIfMissing = true)
    fun threadLocalFilter(): FilterRegistrationBean<ThreadLocalFilter> {
        return FilterRegistrationBean(ThreadLocalFilter()).apply {
            addUrlPatterns("/*")
            setName(ThreadLocalFilter.BEAN_NAME)
            isAsyncSupported = true
            order = 2
        }
    }
}
