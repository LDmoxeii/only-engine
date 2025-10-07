package com.only.engine.web.config

import com.only.engine.web.WebInitPrinter
import com.only.engine.web.config.properties.WebProperties
import com.only.engine.web.interceptor.WebPerformanceInterceptor
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
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
class WebMvcConfiguration(
    private val webProperties: WebProperties,
) : WebMvcConfigurer, WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(WebMvcConfiguration::class.java)
    }

    init {
        printInit(WebMvcConfiguration::class.java, log)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        // 性能拦截器
        if (webProperties.performanceInterceptor.enable) {
            registry.addInterceptor(WebPerformanceInterceptor(webProperties))
        }
    }
}
