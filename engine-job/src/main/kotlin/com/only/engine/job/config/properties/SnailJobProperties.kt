package com.only.engine.job.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("only.engine.job")
class SnailJobProperties {

    var enable: Boolean = false
}
