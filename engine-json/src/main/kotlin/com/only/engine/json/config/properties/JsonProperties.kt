package com.only.engine.json.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "only.engine.json")
class JsonProperties {

    var enable: Boolean = true
}
