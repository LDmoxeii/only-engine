package com.only.engine.error

interface ErrorCode {
    val code: Int
    val name: String
    val message: String
    val category: ErrorCategory
}

interface BusinessErrorCode : ErrorCode {
    override val category: ErrorCategory
        get() = ErrorCategory.BUSINESS
}

interface RequestErrorCode : ErrorCode {
    override val category: ErrorCategory
        get() = ErrorCategory.REQUEST
}

interface AuthenticationErrorCode : ErrorCode {
    override val category: ErrorCategory
        get() = ErrorCategory.AUTHENTICATION
}

interface AuthorizationErrorCode : ErrorCode {
    override val category: ErrorCategory
        get() = ErrorCategory.AUTHORIZATION
}

interface RateLimitErrorCode : ErrorCode {
    override val category: ErrorCategory
        get() = ErrorCategory.RATE_LIMIT
}

interface SystemErrorCode : ErrorCode {
    override val category: ErrorCategory
        get() = ErrorCategory.SYSTEM
}

interface DependencyErrorCode : ErrorCode {
    override val category: ErrorCategory
        get() = ErrorCategory.DEPENDENCY
}
