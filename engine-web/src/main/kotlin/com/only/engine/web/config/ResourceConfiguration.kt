package com.only.engine.web.config

import com.only.engine.web.WebInitPrinter
import com.only.engine.web.config.properties.WebProperties
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
@EnableConfigurationProperties(WebProperties::class)
class ResourceConfiguration(
    private val webProperties: WebProperties,
) : WebMvcConfigurer, WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(ResourceConfiguration::class.java)
        const val CORS_FILTER_BEAN_NAME = "corsFilter"
    }

    init {
        printInit(ResourceConfiguration::class.java, log)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        if (webProperties.performanceInterceptor.enable) {
            registry.addInterceptor(WebPerformanceInterceptor(webProperties))
        }
    }

    /**
     * 创建 CORS 过滤器
     */
    @Bean(CORS_FILTER_BEAN_NAME)
    @ConditionalOnMissingBean(name = [CORS_FILTER_BEAN_NAME])
    @ConditionalOnProperty(prefix = "only.web.cors", name = ["enable"], havingValue = "false", matchIfMissing = false)
    fun corsFilter(): CorsFilter {
        val config = CorsConfiguration().apply {
            // 设置是否允许发送凭证
            allowCredentials = webProperties.cors.allowCredentials

            // 设置允许的源模式
            webProperties.cors.allowedOriginPatterns.forEach { addAllowedOriginPattern(it) }

            // 设置允许的请求头
            webProperties.cors.allowedHeaders.forEach { addAllowedHeader(it) }

            // 设置允许的请求方法
            webProperties.cors.allowedMethods.forEach { addAllowedMethod(it) }

            // 设置预检请求缓存时间
            maxAge = webProperties.cors.maxAge
        }

        // 创建 CORS 配置源
        val source = UrlBasedCorsConfigurationSource().apply {
            // 拦截所有路径
            registerCorsConfiguration("/**", config)
        }

        return CorsFilter(source)
    }
}
