package com.only.engine.exception

import com.only.engine.error.ErrorCategory
import com.only.engine.error.SystemErrorCode

class SystemException(
    errorCode: SystemErrorCode,
    message: String = errorCode.message,
    context: Map<String, Any?> = emptyMap(),
    cause: Throwable? = null,
) : AppException(errorCode, ErrorCategory.SYSTEM, message, context, cause)
