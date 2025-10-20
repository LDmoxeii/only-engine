package com.only.engine.redis.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "only.engine.redis")
class RedisProperties(

    var enable: Boolean = false,

    var provider: ProviderConfig = ProviderConfig(),
) {
    /**
     * SPI 提供商配置
     */
    class ProviderConfig {

        var tokenProvider: String = ""
    }
}
