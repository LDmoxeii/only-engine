package com.only.engine.web.advice

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonProcessingException
import com.only.engine.annotation.RespStatus
import com.only.engine.constants.StandardCode
import com.only.engine.entity.Result
import com.only.engine.enums.HttpStatus
import com.only.engine.exception.ErrorException
import com.only.engine.exception.KnownException
import com.only.engine.exception.WarnException
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.ConstraintViolationException
import org.apache.catalina.connector.ClientAbortException
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.annotation.Order
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

/**
 * 全局异常处理器
 */
@Order(3)
@AutoConfiguration
@RestControllerAdvice
@ConditionalOnProperty(prefix = "only.engine.web.exception-handler", name = ["enable"], havingValue = "true")
class GlobalExceptionHandlerAdvice() {

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandlerAdvice::class.java)
    }

    /**
     * 找不到路由
     */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(ex: NoHandlerFoundException, request: HttpServletRequest): Result<Unit> {
        val requestURI = request.requestURI
        logError(request, "请求地址'$requestURI'不存在", ex)
        return Result.error(StandardCode.UserSide.Exception.code, "请求地址不存在")
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest,
    ): Result<Unit> {
        val requestURI = request.requestURI
        logWarning(request, "请求地址'$requestURI',不支持'${ex.method}'请求", ex)
        return Result.error(StandardCode.UserSide.Exception.code, "不支持的请求方法")
    }

    /**
     * 缺少请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(
        ex: MissingServletRequestParameterException,
        request: HttpServletRequest,
    ): Result<Unit> {
        val message = "缺少必要的请求参数: ${ex.parameterName}"
        logWarning(request, message, ex)
        return Result.error(StandardCode.UserSide.REQUEST_PARAMETER_EXCEPTION, message)
    }

    /**
     * 请求参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest,
    ): Result<Unit> {
        val message =
            "请求参数类型不匹配，参数[${ex.name}]要求类型为：'${ex.requiredType?.name}',但输入值为：'${ex.value}'"
        logWarning(request, message, ex)
        return Result.error(StandardCode.UserSide.REQUEST_PARAMETER_EXCEPTION, message)
    }

    /**
     * 缺少路径变量
     */
    @ExceptionHandler(MissingPathVariableException::class)
    fun handleMissingPathVariable(
        ex: MissingPathVariableException,
        request: HttpServletRequest,
    ): Result<Unit> {
        val message = "请求路径中缺少必需的路径变量[${ex.variableName}]"
        logError(request, message, ex)
        return Result.error(StandardCode.UserSide.REQUEST_PARAMETER_EXCEPTION, message)
    }

    /**
     * 缺少 Header 参数
     */
    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleMissingHeader(
        ex: MissingRequestHeaderException,
        request: HttpServletRequest,
    ): Result<Unit> {
        val message = "缺少必要的 Header 参数: ${ex.headerName}"
        logWarning(request, message, ex)
        return Result.error(StandardCode.UserSide.REQUEST_PARAMETER_EXCEPTION, message)
    }

    @ExceptionHandler(HttpClientErrorException::class, HttpServerErrorException::class)
    fun restTemplateException(ex: Exception, request: HttpServletRequest): Result<Unit> {
        logError(request, ex.message ?: "未知错误", ex)
        return Result.error(StandardCode.ThirdParty.Exception)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        ex: ConstraintViolationException,
        request: HttpServletRequest,
    ): Result<Unit> {
        val errors = ex.constraintViolations.joinToString(", ") { "${it.propertyPath}: ${it.message}" }
        val msg = "请求参数无效: $errors"
        logWarning(request, msg, ex)
        return Result.error(StandardCode.UserSide.REQUEST_PARAMETER_EXCEPTION, msg)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validationMethodArgumentException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): Result<Unit> = validationBindException(ex, request)

    @ExceptionHandler(BindException::class)
    fun validationBindException(ex: BindException, request: HttpServletRequest): Result<Unit> {
        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}：${it.defaultMessage}" }
        val msg = "请求参数无效: $errors"
        logWarning(request, msg, ex)
        return Result.error(StandardCode.UserSide.REQUEST_PARAMETER_EXCEPTION, msg)
    }

    @ExceptionHandler(
        HttpMessageNotReadableException::class,
        JsonProcessingException::class,
        JsonParseException::class,
    )
    fun handleJsonPayloadException(
        ex: Exception,
        request: HttpServletRequest,
    ): Result<Unit> {
        val message = ex.message ?: "参数格式不匹配"
        logWarning(request, message, ex)
        return Result.error(StandardCode.UserSide.RequestParameterFormatNotMatch)
    }

    @ExceptionHandler(ErrorException::class)
    fun handleErrorException(
        ex: ErrorException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = handleKnownExceptionInternal(ex, request, response, HttpStatus.INTERNAL_SERVER_ERROR)

    @ExceptionHandler(WarnException::class)
    fun handleWarnException(
        ex: WarnException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = handleKnownExceptionInternal(ex, request, response, HttpStatus.BAD_REQUEST)

    @ExceptionHandler(KnownException::class)
    fun handleKnownException(
        ex: KnownException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = handleKnownExceptionInternal(ex, request, response, HttpStatus.BAD_REQUEST)

    @ExceptionHandler(IllegalArgumentException::class)
    fun illegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest,
    ): Result<Unit> {
        val message = ex.message ?: "参数错误"
        logWarning(request, message, ex)
        return Result.error(StandardCode.UserSide.RequestParameterException)
    }

    @ExceptionHandler(ClientAbortException::class)
    fun handleClientAbortException(
        ex: ClientAbortException,
        request: HttpServletRequest,
    ): Result<Unit>? {
        if (log.isDebugEnabled) {
            log.debug(
                "Path: [{}], Client aborted connection: [{}]",
                request.requestURI,
                ex.message ?: ex.javaClass.simpleName,
            )
        }
        return null
    }

    /**
     * Servlet 异常
     */
    @ExceptionHandler(ServletException::class)
    fun handleServletException(
        ex: ServletException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> {
        val requestURI = request.requestURI
        logError(request, "请求地址'$requestURI',发生未知异常", ex)
        handlerResponseStatus(ex, response, HttpStatus.INTERNAL_SERVER_ERROR)
        return Result.error(StandardCode.SystemSide.Exception)
    }

    /**
     * IO 异常处理
     * 特殊处理 SSE 连接中断
     */
    @ExceptionHandler(IOException::class)
    fun handleIOException(
        ex: IOException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit>? {
        val requestURI = request.requestURI
        if (requestURI.contains("sse", ignoreCase = true)) {
            if (log.isDebugEnabled) {
                log.debug(
                    "Path: [{}], SSE connection closed by client: [{}]",
                    requestURI,
                    ex.message ?: "连接中断",
                )
            }
            return null
        }
        logError(request, "请求地址'$requestURI',连接中断", ex)
        handlerResponseStatus(ex, response, HttpStatus.INTERNAL_SERVER_ERROR)
        return Result.error(StandardCode.SystemSide.Exception)
    }

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(
        ex: Throwable,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> {
        val message = resolveUserMessage(ex, StandardCode.SystemSide.Exception.message)
        logError(request, message, ex)
        handlerResponseStatus(ex, response, HttpStatus.INTERNAL_SERVER_ERROR)
        return Result.error(StandardCode.SystemSide.EXCEPTION, message)
    }

    /**
     * 判断是否为敏感异常（不应该暴露给用户的内部异常）
     */
    private fun isSensitiveException(ex: Throwable): Boolean {
        return when (ex) {
            is NullPointerException,
            is ClassCastException,
            is NoSuchMethodException -> true

            else -> {
                val msg = ex.message ?: return false
                msg.contains("database", ignoreCase = true) ||
                        msg.contains("sql", ignoreCase = true) ||
                        msg.contains("connection", ignoreCase = true)
            }
        }
    }

    private fun resolveUserMessage(ex: Throwable, fallback: String): String {
        val message = ex.message
        if (message.isNullOrBlank()) {
            return fallback
        }
        return if (isSensitiveException(ex)) fallback else message
    }

    /**
     * 日志工具函数
     */
    private fun logInfo(request: HttpServletRequest, message: String, e: Throwable) {
        log.info(
            "Path: [{}], Exception message: [{}], Exception: [{}]",
            request.requestURI, message, e::class.simpleName,
        )
    }

    private fun logWarning(request: HttpServletRequest, message: String, e: Throwable) {
        log.warn(
            "Path: [{}], Exception message: [{}], Exception: [{}]",
            request.requestURI, message, e::class.simpleName,
        )
    }

    private fun logError(request: HttpServletRequest, message: String, e: Throwable) {
        log.error(
            "Path: [{}], Exception message: [{}], Exception: [{}]",
            request.requestURI, message, e::class.simpleName, e,
        )
    }

    /**
     * 响应状态处理
     */
    private fun handlerResponseStatus(
        ex: Throwable,
        response: HttpServletResponse,
        defStatus: HttpStatus? = null,
    ) {
        if (response.isCommitted) {
            return
        }
        val respStatus = ex::class.java.getDeclaredAnnotation(RespStatus::class.java)
        val status = respStatus?.value ?: defStatus ?: HttpStatus.INTERNAL_SERVER_ERROR
        response.status = status.value
    }

    private fun handleKnownExceptionInternal(
        ex: KnownException,
        request: HttpServletRequest,
        response: HttpServletResponse,
        defaultStatus: HttpStatus,
    ): Result<Unit> {
        val message = ex.msg.ifBlank { StandardCode.SystemSide.Exception.message }
        when (ex.level) {
            org.slf4j.event.Level.ERROR -> logError(request, message, ex)
            org.slf4j.event.Level.WARN -> logWarning(request, message, ex)
            else -> logInfo(request, message, ex)
        }
        handlerResponseStatus(ex, response, defaultStatus)
        return Result.error(ex.code, message)
    }
}
