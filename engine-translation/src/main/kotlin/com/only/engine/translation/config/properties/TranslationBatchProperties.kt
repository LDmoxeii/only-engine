package com.only.engine.translation.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "only.engine.translation.batch")
class TranslationBatchProperties {
    var enable: Boolean = true
    var threshold: Int = 8
    var cacheEnabled: Boolean = true
    var maxKeysPerGroup: Int = 2000
}
