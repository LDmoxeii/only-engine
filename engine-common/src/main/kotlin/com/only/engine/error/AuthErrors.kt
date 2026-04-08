package com.only.engine.error

enum class AuthErrors(
    override val code: Int,
    override val message: String,
    override val category: ErrorCategory,
) : ErrorCode {
    LOGIN_REQUIRED(40100, "未登录", ErrorCategory.AUTHENTICATION),
    ACCESS_DENIED(40300, "无权限访问", ErrorCategory.AUTHORIZATION),
    CAPTCHA_INVALID(40240, "验证码错误", ErrorCategory.BUSINESS),
}
