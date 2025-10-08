package com.only.engine.json.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "only.engine.json")
class JsonProperties {
    /**
     * 是否启用 JSON 功能
     */
    var enable: Boolean = true
}
