package com.only.engine.error

enum class CommonErrors(
    override val code: Int,
    override val message: String,
    override val category: ErrorCategory,
) : ErrorCode {
    BUSINESS_ERROR(400500, "业务异常", ErrorCategory.BUSINESS),
    PARAM_INVALID(40400, "请求参数异常", ErrorCategory.REQUEST),
    PARAM_REQUIRED(40401, "参数不能为空", ErrorCategory.REQUEST),
    REQUEST_RATE_LIMITED(40501, "请求过于频繁", ErrorCategory.RATE_LIMIT),
    SYSTEM_ERROR(50000, "系统异常", ErrorCategory.SYSTEM),
    DEPENDENCY_ERROR(60000, "第三方服务异常", ErrorCategory.DEPENDENCY),
}
