package com.only.engine.web.config

import com.only.engine.web.WebInitPrinter
import com.only.engine.web.config.properties.CorsProperties
import com.only.engine.web.config.properties.PerformanceInterceptorProperties
import com.only.engine.web.interceptor.WebPerformanceInterceptor
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web MVC 配置
 *
 * 配置拦截器、资源处理器等
 *
 * @author LD_moxeii
 */
@AutoConfiguration
@EnableConfigurationProperties(CorsProperties::class, PerformanceInterceptorProperties::class)
class ResourceAutoConfiguration(
    private val corsProperties: CorsProperties,
    private val performanceInterceptorProperties: PerformanceInterceptorProperties,
) : WebMvcConfigurer, WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(ResourceAutoConfiguration::class.java)
        const val CORS_FILTER_BEAN_NAME = "corsFilter"
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        if (performanceInterceptorProperties.enable) {
            registry.addInterceptor(WebPerformanceInterceptor(performanceInterceptorProperties.slowRequestThreshold))
        }
    }

    /**
     * 创建 CORS 过滤器
     */
    @Bean(CORS_FILTER_BEAN_NAME)
    @ConditionalOnMissingBean(name = [CORS_FILTER_BEAN_NAME])
    @ConditionalOnProperty(prefix = "only.engine.web.cors", name = ["enable"], havingValue = "true")
    fun corsFilter(): CorsFilter {
        val config = CorsConfiguration().apply {
            // 设置是否允许发送凭证
            allowCredentials = corsProperties.allowCredentials

            // 设置允许的源模式
            corsProperties.allowedOriginPatterns.forEach { addAllowedOriginPattern(it) }

            // 设置允许的请求头
            corsProperties.allowedHeaders.forEach { addAllowedHeader(it) }

            // 设置允许的请求方法
            corsProperties.allowedMethods.forEach { addAllowedMethod(it) }

            // 设置预检请求缓存时间
            maxAge = corsProperties.maxAge
        }

        // 创建 CORS 配置源
        val source = UrlBasedCorsConfigurationSource().apply {
            // 拦截所有路径
            registerCorsConfiguration("/**", config)
        }

        printInit(CorsFilter::class.java, log)

        return CorsFilter(source)
    }
}
