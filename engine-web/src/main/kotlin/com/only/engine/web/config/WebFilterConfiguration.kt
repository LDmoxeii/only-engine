package com.only.engine.web.config

import com.only.engine.web.WebInitPrinter
import com.only.engine.web.config.properties.FilterProperties
import com.only.engine.web.filter.HealthCheckFilter
import com.only.engine.web.filter.RequestBodyWrapperFilter
import com.only.engine.web.filter.ThreadLocalFilter
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered

@AutoConfiguration
@EnableConfigurationProperties(FilterProperties::class)
class WebFilterConfiguration(
    private val filterProperties: FilterProperties,
) : WebInitPrinter {

    /**
     * 健康检查过滤器
     */
    @Bean(HealthCheckFilter.BEAN_NAME)
    @ConditionalOnMissingBean(name = [HealthCheckFilter.BEAN_NAME])
    @ConditionalOnProperty(prefix = "only.engine.web.filter.health-check", name = ["enable"], havingValue = "true")
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
    @ConditionalOnProperty(prefix = "only.engine.web.filter.request-body", name = ["enable"], havingValue = "true")
    fun requestBodyWrapperFilter(): FilterRegistrationBean<RequestBodyWrapperFilter> {
        val skipPath = filterProperties.userLogin.skipPaths.apply {
            addAll(filterProperties.requestBody.filterUris)
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
    @ConditionalOnProperty(prefix = "only.engine.web.filter.thread-local", name = ["enable"], havingValue = "true")
    fun threadLocalFilter(): FilterRegistrationBean<ThreadLocalFilter> {
        return FilterRegistrationBean(ThreadLocalFilter()).apply {
            addUrlPatterns("/*")
            setName(ThreadLocalFilter.BEAN_NAME)
            isAsyncSupported = true
            order = 2
        }
    }
}
