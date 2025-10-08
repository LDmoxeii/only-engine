package com.only.engine.jimmer.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Jimmer 配置属性
 *
 * @author LD_moxeii
 */
@ConfigurationProperties(prefix = "only.engine.jimmer")
class JimmerProperties {
    /**
     * 是否启用 Jimmer
     */
    var enable: Boolean = false
}
