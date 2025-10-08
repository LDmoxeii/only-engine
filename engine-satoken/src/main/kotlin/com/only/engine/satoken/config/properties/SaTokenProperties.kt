package com.only.engine.satoken.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "only.engine.satoken")
class SaTokenProperties {
    /**
     * 是否启用 Sa-Token
     */
    var enable: Boolean = false
}
