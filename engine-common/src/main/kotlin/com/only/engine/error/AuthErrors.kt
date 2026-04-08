package com.only.engine.error

object AuthErrors {
    object LOGIN_REQUIRED : AuthenticationErrorCode(40100, "LOGIN_REQUIRED", "未登录")

    object ACCESS_DENIED : AuthorizationErrorCode(40300, "ACCESS_DENIED", "无权限访问")

    object CAPTCHA_INVALID : BusinessErrorCode(40240, "CAPTCHA_INVALID", "验证码错误")
}
