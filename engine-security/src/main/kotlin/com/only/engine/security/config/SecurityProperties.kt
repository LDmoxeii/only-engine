package com.only.engine.security.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(prefix = "only.security")
data class SecurityProperties(
    @DefaultValue("true")
    val enable: Boolean = true,
    @DefaultValue("sa-token")
    val provider: String = "sa-token",
    val excludes: Array<String> = arrayOf(
        "/favicon.ico",
        "/actuator/**",
        "/error",
        "/swagger-ui/**",
        "/swagger-resources/**",
        "/webjars/**",
        "/v3/api-docs/**"
    ),
    val token: TokenProperties = TokenProperties(),
    val cache: CacheProperties = CacheProperties(),
) {

    data class TokenProperties(
        @DefaultValue("7200")
        val timeout: Long = 7200,
        @DefaultValue("1800")
        val activeTimeout: Long = 1800,
        @DefaultValue("Authorization")
        val tokenName: String = "Authorization",
        @DefaultValue("Bearer ")
        val tokenPrefix: String = "Bearer ",
        @DefaultValue("true")
        val allowConcurrentLogin: Boolean = true,
    )

    data class CacheProperties(
        @DefaultValue("local")
        val type: String = "local", // local, redis
        @DefaultValue("5")
        val cleanupIntervalMinutes: Long = 5,
        @DefaultValue("1000")
        val maxSize: Int = 1000,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SecurityProperties

        if (enable != other.enable) return false
        if (provider != other.provider) return false
        if (!excludes.contentEquals(other.excludes)) return false
        if (token != other.token) return false
        if (cache != other.cache) return false

        return true
    }

    override fun hashCode(): Int {
        var result = enable.hashCode()
        result = 31 * result + provider.hashCode()
        result = 31 * result + excludes.contentHashCode()
        result = 31 * result + token.hashCode()
        result = 31 * result + cache.hashCode()
        return result
    }
}
