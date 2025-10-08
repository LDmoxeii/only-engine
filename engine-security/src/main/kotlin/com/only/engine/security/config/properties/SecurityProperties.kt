package com.only.engine.security.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "only.engine.security")
data class SecurityProperties(
    val enable: Boolean = false,
    val provider: String = "",
    val excludes: Array<String> = arrayOf(
        "/favicon.ico",
        "/actuator/**",
        "/error",
        "/swagger-ui/**",
        "/swagger-resources/**",
        "/webjars/**",
        "/v3/api-docs/**"
    ),
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SecurityProperties

        if (enable != other.enable) return false
        if (provider != other.provider) return false
        if (!excludes.contentEquals(other.excludes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = enable.hashCode()
        result = 31 * result + provider.hashCode()
        result = 31 * result + excludes.contentHashCode()
        return result
    }
}
