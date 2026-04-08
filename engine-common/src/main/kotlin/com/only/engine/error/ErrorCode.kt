package com.only.engine.error

interface ErrorCode {
    val code: Int
    val name: String
    val message: String
    val category: ErrorCategory
}

abstract class BusinessErrorCode(
    final override val code: Int,
    final override val name: String,
    final override val message: String,
) : ErrorCode {
    final override val category: ErrorCategory = ErrorCategory.BUSINESS
}

abstract class RequestErrorCode(
    final override val code: Int,
    final override val name: String,
    final override val message: String,
) : ErrorCode {
    final override val category: ErrorCategory = ErrorCategory.REQUEST
}

abstract class AuthenticationErrorCode(
    final override val code: Int,
    final override val name: String,
    final override val message: String,
) : ErrorCode {
    final override val category: ErrorCategory = ErrorCategory.AUTHENTICATION
}

abstract class AuthorizationErrorCode(
    final override val code: Int,
    final override val name: String,
    final override val message: String,
) : ErrorCode {
    final override val category: ErrorCategory = ErrorCategory.AUTHORIZATION
}

abstract class RateLimitErrorCode(
    final override val code: Int,
    final override val name: String,
    final override val message: String,
) : ErrorCode {
    final override val category: ErrorCategory = ErrorCategory.RATE_LIMIT
}

abstract class SystemErrorCode(
    final override val code: Int,
    final override val name: String,
    final override val message: String,
) : ErrorCode {
    final override val category: ErrorCategory = ErrorCategory.SYSTEM
}

abstract class DependencyErrorCode(
    final override val code: Int,
    final override val name: String,
    final override val message: String,
) : ErrorCode {
    final override val category: ErrorCategory = ErrorCategory.DEPENDENCY
}
