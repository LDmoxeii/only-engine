package com.only.engine.exception

import com.only.engine.error.ErrorCategory
import com.only.engine.error.ErrorCode

class RateLimitException(
    errorCode: ErrorCode,
    message: String = errorCode.message,
    context: Map<String, Any?> = emptyMap(),
    cause: Throwable? = null,
) : AppException(errorCode, ErrorCategory.RATE_LIMIT, message, context, cause)
