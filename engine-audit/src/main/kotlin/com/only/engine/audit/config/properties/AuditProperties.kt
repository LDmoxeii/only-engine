package com.only.engine.audit.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "only.engine.audit")
class AuditProperties {
    var enable: Boolean = true
    var createUserIdField: String = "createUserId"
    var createByField: String = "createBy"
    var createTimeField: String = "createTime"
    var updateUserIdField: String = "updateUserId"
    var updateByField: String = "updateBy"
    var updateTimeField: String = "updateTime"
}
