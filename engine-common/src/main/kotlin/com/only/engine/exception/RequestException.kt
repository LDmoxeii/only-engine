package com.only.engine.exception

import com.only.engine.error.ErrorCategory
import com.only.engine.error.RequestErrorCode

class RequestException(
    errorCode: RequestErrorCode,
    message: String = errorCode.message,
    context: Map<String, Any?> = emptyMap(),
    cause: Throwable? = null,
) : AppException(errorCode, ErrorCategory.REQUEST, message, context, cause)
