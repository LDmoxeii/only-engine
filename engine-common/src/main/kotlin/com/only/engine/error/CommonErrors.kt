package com.only.engine.error

object CommonErrors {
    object BUSINESS_ERROR : BusinessErrorCode(400500, "BUSINESS_ERROR", "业务异常")

    object PARAM_INVALID : RequestErrorCode(40400, "PARAM_INVALID", "请求参数异常")

    object PARAM_REQUIRED : RequestErrorCode(40401, "PARAM_REQUIRED", "参数不能为空")

    object REQUEST_RATE_LIMITED : RateLimitErrorCode(40501, "REQUEST_RATE_LIMITED", "请求过于频繁")

    object SYSTEM_ERROR : SystemErrorCode(50000, "SYSTEM_ERROR", "系统异常")

    object DEPENDENCY_ERROR : DependencyErrorCode(60000, "DEPENDENCY_ERROR", "第三方服务异常")
}
