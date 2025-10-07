package com.only.engine.web.config

import com.only.engine.web.WebInitPrinter
import com.only.engine.web.config.properties.WebProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import org.springframework.web.cors.CorsConfiguration as SpringCorsConfiguration

/**
 * CORS 跨域配置
 *
 * 提供全局的 CORS 跨域资源共享配置
 *
 * @author LD_moxeii
 */
@AutoConfiguration
@EnableConfigurationProperties(WebProperties::class)
@ConditionalOnProperty(prefix = "only.web.cors", name = ["enable"], matchIfMissing = true)
class CorsConfiguration(
    private val webProperties: WebProperties,
) : WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(CorsConfiguration::class.java)
        const val CORS_FILTER_BEAN_NAME = "corsFilter"
    }

    init {
        printInit(CorsConfiguration::class.java, log)
    }

    /**
     * 创建 CORS 过滤器
     */
    @Bean(CORS_FILTER_BEAN_NAME)
    @ConditionalOnMissingBean(name = [CORS_FILTER_BEAN_NAME])
    fun corsFilter(): CorsFilter {
        val config = SpringCorsConfiguration().apply {
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
