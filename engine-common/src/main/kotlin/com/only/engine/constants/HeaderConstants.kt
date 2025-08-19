package com.only.engine.constants

object HeaderConstants {
    const val AUTHENTICATION_HEADER_VALUE_PREFIX = "Bearer "

    /**
     * 用于标记请求是内部RPC请求
     */
    const val X_ONLY_FROM_IN = "X-ONLY-From-In"

    /**
     * 租户Id请求头
     */
    const val X_ONLY_TENANT_ID = "X-ONLY-Tenant-Id"

    /**
     * 前端域名请求头
     */
    const val X_ONLY_ORIGIN_DOMAIN = "X-ONLY-Origin-Domain"

    /**
     * 选择切换租户ID
     */
    const val X_ONLY_SWITCH_TENANT_ID = "X-ONLY-Switch-Tenant-Id"

    /**
     * 选择切换租户下的客户ID
     */
    const val X_ONLY_SWITCH_CUSTOMER_ID = "X-ONLY-Switch-Customer-Id"

    /**
     * 当前操作用户上下文信息
     */
    const val X_ONLY_USER_CONTEXT = "X-ONLY-User-Context"

    // Thread local 存储
    /**
     * jwt
     */
    const val X_ONLY_JWT_INFO = "X-ONLY-Jwt-Info"

    /**
     * token info
     */
    const val X_ONLY_TOKEN_INFO = "X-ONLY-Token-Info"

    /**
     * 国际化信息KEY
     */
    const val X_ONLY_LOCALE_INFO = "X-ONLY-Locale-Info"

    /**
     * 单次请求服务追踪码
     */
    const val X_ONLY_BIZ_TRACK_CODE = "X-ONLY-Biz-Track-Code"
}
