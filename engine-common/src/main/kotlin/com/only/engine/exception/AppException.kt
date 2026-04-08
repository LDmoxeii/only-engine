package com.only.engine.exception

import com.only.engine.error.ErrorCategory
import com.only.engine.error.ErrorCode

abstract class AppException(
    val errorCode: ErrorCode,
    private val expectedCategory: ErrorCategory,
    override val message: String = errorCode.message,
    val context: Map<String, Any?> = emptyMap(),
    cause: Throwable? = null,
) : RuntimeException(message, cause) {

    init {
        require(errorCode.category == expectedCategory) {
            "ErrorCode category ${errorCode.category} cannot be used by ${this::class.simpleName}"
        }
    }
}
