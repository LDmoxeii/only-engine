package com.only.engine.web.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 国际化配置
 */
@ConfigurationProperties(prefix = "only.web.i18n")
data class I18nProperties(
    /** 是否启用国际化，默认 false */
    var enable: Boolean = false,
)
