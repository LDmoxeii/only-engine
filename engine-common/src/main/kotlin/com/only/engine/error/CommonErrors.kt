package com.only.engine.error

object CommonErrors {
    object BUSINESS_ERROR : BusinessErrorCode {
        override val code: Int = 400500
        override val name: String = "BUSINESS_ERROR"
        override val message: String = "业务异常"
    }

    object PARAM_INVALID : RequestErrorCode {
        override val code: Int = 40400
        override val name: String = "PARAM_INVALID"
        override val message: String = "请求参数异常"
    }

    object PARAM_REQUIRED : RequestErrorCode {
        override val code: Int = 40401
        override val name: String = "PARAM_REQUIRED"
        override val message: String = "参数不能为空"
    }

    object REQUEST_RATE_LIMITED : RateLimitErrorCode {
        override val code: Int = 40501
        override val name: String = "REQUEST_RATE_LIMITED"
        override val message: String = "请求过于频繁"
    }

    object SYSTEM_ERROR : SystemErrorCode {
        override val code: Int = 50000
        override val name: String = "SYSTEM_ERROR"
        override val message: String = "系统异常"
    }

    object DEPENDENCY_ERROR : DependencyErrorCode {
        override val code: Int = 60000
        override val name: String = "DEPENDENCY_ERROR"
        override val message: String = "第三方服务异常"
    }
}
