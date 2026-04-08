package com.only.engine.web.advice

import com.only.engine.entity.Result
import com.only.engine.error.ErrorCategory
import com.only.engine.error.ErrorCode
import com.only.engine.misc.ThreadLocalUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.springframework.http.HttpStatus

internal object ExceptionResponseSupport {

    fun write(
        log: Logger,
        category: ErrorCategory,
        request: HttpServletRequest,
        response: HttpServletResponse,
        errorCode: ErrorCode,
        message: String,
        ex: Throwable,
    ): Result<Unit> {
        response.status = resolveStatus(category).value()
        log(log, category, request, message, ex)
        return Result(
            code = errorCode.code,
            message = message,
            requestId = ThreadLocalUtils.getBizTrackCodeOrNull(),
            path = request.requestURI,
        )
    }

    private fun resolveStatus(category: ErrorCategory): HttpStatus =
        when (category) {
            ErrorCategory.BUSINESS -> HttpStatus.OK
            ErrorCategory.REQUEST -> HttpStatus.BAD_REQUEST
            ErrorCategory.AUTHENTICATION -> HttpStatus.UNAUTHORIZED
            ErrorCategory.AUTHORIZATION -> HttpStatus.FORBIDDEN
            ErrorCategory.RATE_LIMIT -> HttpStatus.TOO_MANY_REQUESTS
            ErrorCategory.SYSTEM -> HttpStatus.INTERNAL_SERVER_ERROR
            ErrorCategory.DEPENDENCY -> HttpStatus.SERVICE_UNAVAILABLE
        }

    private fun log(
        log: Logger,
        category: ErrorCategory,
        request: HttpServletRequest,
        message: String,
        ex: Throwable,
    ) {
        when (category) {
            ErrorCategory.BUSINESS, ErrorCategory.AUTHENTICATION -> {
                log.info(
                    "Path: [{}], Exception message: [{}], Exception: [{}]",
                    request.requestURI,
                    message,
                    ex::class.simpleName,
                )
            }
            ErrorCategory.REQUEST, ErrorCategory.AUTHORIZATION, ErrorCategory.RATE_LIMIT -> {
                log.warn(
                    "Path: [{}], Exception message: [{}], Exception: [{}]",
                    request.requestURI,
                    message,
                    ex::class.simpleName,
                )
            }
            ErrorCategory.SYSTEM, ErrorCategory.DEPENDENCY -> {
                log.error(
                    "Path: [{}], Exception message: [{}], Exception: [{}]",
                    request.requestURI,
                    message,
                    ex::class.simpleName,
                    ex,
                )
            }
        }
    }
}
