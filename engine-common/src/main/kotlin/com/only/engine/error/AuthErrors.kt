package com.only.engine.error

object AuthErrors {
    object LOGIN_REQUIRED : AuthenticationErrorCode {
        override val code: Int = 40100
        override val name: String = "LOGIN_REQUIRED"
        override val message: String = "未登录"
    }

    object ACCESS_DENIED : AuthorizationErrorCode {
        override val code: Int = 40300
        override val name: String = "ACCESS_DENIED"
        override val message: String = "无权限访问"
    }

    object CAPTCHA_INVALID : BusinessErrorCode {
        override val code: Int = 40240
        override val name: String = "CAPTCHA_INVALID"
        override val message: String = "验证码错误"
    }
}
