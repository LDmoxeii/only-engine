package com.only.engine.security.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "only.engine.security")
class SecurityProperties {

    /**
     * 是否启用安全拦截器
     */
    var enable: Boolean = false

    /**
     * 排除的路径列表
     */
    var excludes: Array<String> = arrayOf(
        "/favicon.ico",
        "/actuator/**",
        "/error",
        "/swagger-ui/**",
        "/swagger-resources/**",
        "/webjars/**",
        "/v3/api-docs/**"
    )

    /**
     * SPI 提供商配置
     */
    var provider: ProviderConfig = ProviderConfig()

    /**
     * SPI 提供商配置
     */
    class ProviderConfig {
        /**
         * 安全拦截器实现类全限定名
         */
        var securityInterceptor: String = ""
    }
}
