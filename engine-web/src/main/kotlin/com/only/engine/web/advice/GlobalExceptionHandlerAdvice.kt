package com.only.engine.web.advice

import cn.dev33.satoken.exception.NotLoginException
import cn.dev33.satoken.exception.NotPermissionException
import cn.dev33.satoken.exception.NotRoleException
import com.baomidou.lock.exception.LockFailureException
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonProcessingException
import com.only.engine.annotation.RespStatus
import com.only.engine.entity.Result
import com.only.engine.error.AuthErrors
import com.only.engine.error.CommonErrors
import com.only.engine.error.ErrorCategory
import com.only.engine.exception.AppException
import com.only.engine.exception.AuthenticationException
import com.only.engine.exception.AuthorizationException
import com.only.engine.exception.DependencyException
import com.only.engine.exception.ErrorException
import com.only.engine.exception.KnownException
import com.only.engine.exception.RateLimitException
import com.only.engine.exception.RequestException
import com.only.engine.exception.SystemException
import com.only.engine.exception.WarnException
import com.only.engine.misc.ThreadLocalUtils
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.ConstraintViolationException
import org.apache.catalina.connector.ClientAbortException
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import java.io.IOException

@Order(3)
@AutoConfiguration
@RestControllerAdvice
@ConditionalOnProperty(prefix = "only.engine.web.exception-handler", name = ["enable"], havingValue = "true")
class GlobalExceptionHandlerAdvice {

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandlerAdvice::class.java)
    }

    @ExceptionHandler(AppException::class)
    fun handleAppException(
        ex: AppException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> {
        val status = resolveStatus(ex)
        response.status = status.value()
        logByCategory(ex.errorCode.category, request, ex.message, ex)
        return buildErrorResult(ex, request)
    }

    /**
     * Transitional bridge for legacy exceptions before Task 3 migration removes remaining throw sites.
     * Keep isolated from AppException flow.
     */
    @ExceptionHandler(ErrorException::class, WarnException::class, KnownException::class)
    fun handleLegacyKnownException(
        ex: KnownException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> {
        val message = ex.msg.ifBlank { CommonErrors.SYSTEM_ERROR.message }
        response.status = resolveLegacyStatus(ex).value()
        logLegacyException(ex, request, message)
        return Result(
            code = ex.code,
            message = message,
            requestId = ThreadLocalUtils.getBizTrackCodeOrNull(),
            path = request.requestURI,
        )
    }

    @ExceptionHandler(
        MissingServletRequestParameterException::class,
        MissingRequestHeaderException::class,
        MissingPathVariableException::class,
        MethodArgumentTypeMismatchException::class,
        MethodArgumentNotValidException::class,
        BindException::class,
        ConstraintViolationException::class,
        HttpMessageNotReadableException::class,
        JsonProcessingException::class,
        JsonParseException::class,
        IllegalArgumentException::class,
        HttpRequestMethodNotSupportedException::class,
        NoHandlerFoundException::class,
    )
    fun handleRequestFailures(
        ex: Exception,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = handleAppException(
        RequestException(CommonErrors.PARAM_INVALID, resolveRequestMessage(ex)),
        request,
        response,
    )

    @ExceptionHandler(NotLoginException::class)
    fun handleNotLogin(
        ex: NotLoginException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = handleAppException(AuthenticationException(AuthErrors.LOGIN_REQUIRED), request, response)

    @ExceptionHandler(NotPermissionException::class, NotRoleException::class)
    fun handleForbidden(
        ex: RuntimeException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = handleAppException(AuthorizationException(AuthErrors.ACCESS_DENIED), request, response)

    @ExceptionHandler(LockFailureException::class)
    fun handleRateLimit(
        ex: LockFailureException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = handleAppException(
        RateLimitException(CommonErrors.REQUEST_RATE_LIMITED, ex.message ?: CommonErrors.REQUEST_RATE_LIMITED.message),
        request,
        response,
    )

    @ExceptionHandler(HttpClientErrorException::class, HttpServerErrorException::class)
    fun handleDependencyHttpError(
        ex: Exception,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = handleAppException(
        DependencyException(CommonErrors.DEPENDENCY_ERROR, CommonErrors.DEPENDENCY_ERROR.message, cause = ex),
        request,
        response,
    )

    @ExceptionHandler(ClientAbortException::class)
    fun handleClientAbort(
        ex: ClientAbortException,
        request: HttpServletRequest,
    ): Result<Unit>? = null

    @ExceptionHandler(ServletException::class, IOException::class, Throwable::class)
    fun handleThrowable(
        ex: Throwable,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = handleAppException(
        SystemException(CommonErrors.SYSTEM_ERROR, resolveSafeSystemMessage(ex), cause = ex),
        request,
        response,
    )

    private fun resolveStatus(ex: AppException): HttpStatus =
        when (ex.errorCode.category) {
            ErrorCategory.BUSINESS -> HttpStatus.OK
            ErrorCategory.REQUEST -> HttpStatus.BAD_REQUEST
            ErrorCategory.AUTHENTICATION -> HttpStatus.UNAUTHORIZED
            ErrorCategory.AUTHORIZATION -> HttpStatus.FORBIDDEN
            ErrorCategory.RATE_LIMIT -> HttpStatus.TOO_MANY_REQUESTS
            ErrorCategory.SYSTEM -> HttpStatus.INTERNAL_SERVER_ERROR
            ErrorCategory.DEPENDENCY -> HttpStatus.SERVICE_UNAVAILABLE
        }

    private fun logByCategory(
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

    private fun resolveRequestMessage(ex: Exception): String =
        when (ex) {
            is MissingServletRequestParameterException -> "缺少必要的请求参数: ${ex.parameterName}"
            is MissingRequestHeaderException -> "缺少必要的请求头: ${ex.headerName}"
            is MissingPathVariableException -> "缺少必要的路径参数: ${ex.variableName}"
            is MethodArgumentTypeMismatchException -> "请求参数类型不匹配: ${ex.name}"
            is MethodArgumentNotValidException, is BindException, is ConstraintViolationException -> "请求参数校验失败"
            is HttpMessageNotReadableException, is JsonProcessingException, is JsonParseException -> "请求体格式错误"
            is HttpRequestMethodNotSupportedException -> "不支持的请求方法"
            is NoHandlerFoundException -> "请求地址不存在"
            is IllegalArgumentException -> CommonErrors.PARAM_INVALID.message
            else -> CommonErrors.PARAM_INVALID.message
        }

    private fun resolveSafeSystemMessage(ex: Throwable): String =
        ex.message
            ?.takeIf { it.isNotBlank() }
            ?.takeUnless { isSensitiveThrowableMessage(ex, it) }
            ?: CommonErrors.SYSTEM_ERROR.message

    private fun isSensitiveThrowableMessage(ex: Throwable, message: String): Boolean =
        when (ex) {
            is NullPointerException,
            is ClassCastException,
            is NoSuchMethodException -> true
            else -> {
                val lower = message.lowercase()
                lower.contains("database") ||
                    lower.contains("sql") ||
                    lower.contains("connection") ||
                    lower.contains("jdbc") ||
                    lower.contains("password")
            }
        }

    private fun resolveLegacyStatus(ex: KnownException): HttpStatus =
        ex::class.java.getDeclaredAnnotation(RespStatus::class.java)
            ?.let { HttpStatus.valueOf(it.value.value) }
            ?: if (ex is ErrorException) {
                HttpStatus.INTERNAL_SERVER_ERROR
            } else {
                HttpStatus.BAD_REQUEST
            }

    private fun logLegacyException(ex: KnownException, request: HttpServletRequest, message: String) {
        when (ex.level) {
            org.slf4j.event.Level.ERROR -> {
                log.error(
                    "Path: [{}], Exception message: [{}], Exception: [{}]",
                    request.requestURI,
                    message,
                    ex::class.simpleName,
                    ex,
                )
            }
            org.slf4j.event.Level.WARN -> {
                log.warn(
                    "Path: [{}], Exception message: [{}], Exception: [{}]",
                    request.requestURI,
                    message,
                    ex::class.simpleName,
                )
            }
            else -> {
                log.info(
                    "Path: [{}], Exception message: [{}], Exception: [{}]",
                    request.requestURI,
                    message,
                    ex::class.simpleName,
                )
            }
        }
    }

    private fun buildErrorResult(ex: AppException, request: HttpServletRequest): Result<Unit> =
        Result(
            code = ex.errorCode.code,
            message = ex.message,
            requestId = ThreadLocalUtils.getBizTrackCodeOrNull(),
            path = request.requestURI,
        )
}
