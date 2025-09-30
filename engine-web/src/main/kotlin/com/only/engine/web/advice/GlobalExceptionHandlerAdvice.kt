package com.only.engine.web.advice

import com.only.engine.annotation.RespStatus
import com.only.engine.constants.StandardCode
import com.only.engine.entity.Result
import com.only.engine.enums.HttpStatus
import com.only.engine.exception.KnownException
import com.only.engine.exception.WarnException
import com.only.engine.web.WebInitPrinter
import com.only.engine.web.WebProperties
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.annotation.Order
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException

/**
 * 全局异常处理器
 * 使用新的StandardCode和配置系统
 */
@Order(3)
@AutoConfiguration
@RestControllerAdvice
@ConditionalOnProperty(prefix = "only.web.exception-handler", name = ["enable"], matchIfMissing = true)
class GlobalExceptionHandlerAdvice(
    private val webProperties: WebProperties
) : WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandlerAdvice::class.java)
    }

    init {
        printInit(GlobalExceptionHandlerAdvice::class.java, log)
    }

    @ExceptionHandler(HttpClientErrorException::class, HttpServerErrorException::class)
    fun restTemplateException(ex: Exception, request: HttpServletRequest): Result<Void> {
        logError(request, ex.message ?: "未知错误", ex)
        return Result.error(StandardCode.ThirdParty.Exception)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        ex: ConstraintViolationException,
        request: HttpServletRequest,
    ): Result<Void> {
        val errors = ex.constraintViolations.joinToString(", ") { "${it.propertyPath}: ${it.message}" }
        val msg = "请求参数无效: $errors"
        logInfo(request, msg, ex)
        return Result.error(StandardCode.UserSide.REQUEST_PARAMETER_EXCEPTION, msg)
    }


    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validationMethodArgumentException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): Result<Void> = validationBindException(ex, request)

    @ExceptionHandler(BindException::class)
    fun validationBindException(ex: BindException, request: HttpServletRequest): Result<Void> {
        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}：${it.defaultMessage}" }
        val msg = "请求参数无效: $errors"
        logInfo(request, msg, ex)
        return Result.error(StandardCode.UserSide.REQUEST_PARAMETER_EXCEPTION, msg)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun httpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Result<Void> {
        val message = "参数格式不匹配"
        logWarning(request, ex.message ?: message, ex)
        return Result.error(StandardCode.UserSide.RequestParameterFormatNotMatch)
    }

    @ExceptionHandler(KnownException::class)
    fun knownException(
        ex: KnownException,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Result<*> {
        when (ex.level) {
            org.slf4j.event.Level.ERROR -> logError(request, ex.message ?: "", ex)
            org.slf4j.event.Level.WARN -> logWarning(request, ex.message ?: "", ex)
            else -> logInfo(request, ex.message ?: "", ex)
        }
        handlerResponseStatus(ex, response)
        return Result.error(ex.code, ex.message ?: "")
    }

    @ExceptionHandler(WarnException::class)
    fun warnException(
        ex: WarnException,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Result<Void> {
        logWarning(request, ex.message ?: "", ex)
        handlerResponseStatus(ex, response)
        return Result.error(ex.code, ex.message ?: "")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun illegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): Result<Void> {
        val message = ex.message ?: "参数错误"
        logWarning(request, message, ex)
        return Result.error(StandardCode.UserSide.RequestParameterException)
    }

    @ExceptionHandler(Exception::class)
    fun exception(ex: Exception, request: HttpServletRequest): Result<Void> {
        val shouldPrintStackTrace = webProperties.exceptionHandler.printStackTrace
        if (shouldPrintStackTrace) {
            logError(request, ex.message ?: "系统异常", ex)
        } else {
            logWarning(request, ex.message ?: "系统异常", ex)
        }

        // 检查是否需要替换敏感信息
        val message = if (isSensitiveException(ex)) {
            webProperties.exceptionHandler.sensitiveMessageReplacement
        } else {
            ex.message ?: "系统异常"
        }

        return Result.error(StandardCode.SystemSide.Exception)
    }

    // ===================== 私有工具方法 ===================== //

    /**
     * 判断是否为敏感异常（不应该暴露给用户的内部异常）
     */
    private fun isSensitiveException(ex: Exception): Boolean {
        return when {
            ex is NullPointerException -> true
            ex is ClassCastException -> true
            ex is NoSuchMethodException -> true
            ex.message?.contains("database", ignoreCase = true) == true -> true
            ex.message?.contains("sql", ignoreCase = true) == true -> true
            ex.message?.contains("connection", ignoreCase = true) == true -> true
            else -> false
        }
    }

    /**
     * 日志工具函数
     */
    private fun logInfo(request: HttpServletRequest, message: String, e: Exception) {
        if (webProperties.exceptionHandler.logLevel.ordinal <= WebProperties.LogLevel.INFO.ordinal) {
            log.info(
                "Path: [{}], Exception message: [{}], Exception: [{}]",
                request.requestURI, message, e::class.simpleName
            )
        }
    }

    private fun logWarning(request: HttpServletRequest, message: String, e: Exception) {
        if (webProperties.exceptionHandler.logLevel.ordinal <= WebProperties.LogLevel.WARN.ordinal) {
            log.warn(
                "Path: [{}], Exception message: [{}], Exception: [{}]",
                request.requestURI, message, e::class.simpleName
            )
        }
    }

    private fun logError(request: HttpServletRequest, message: String, e: Exception) {
        if (webProperties.exceptionHandler.logLevel.ordinal <= WebProperties.LogLevel.ERROR.ordinal) {
            log.error(
                "Path: [{}], Exception message: [{}], Exception: [{}]",
                request.requestURI, message, e::class.simpleName, e
            )
        }
    }

    /**
     * 响应状态处理
     */
    private fun handlerResponseStatus(
        ex: Exception,
        response: HttpServletResponse,
        defStatus: HttpStatus? = null
    ) {
        val respStatus = ex::class.java.getDeclaredAnnotation(RespStatus::class.java)
        val status = respStatus?.value ?: defStatus ?: HttpStatus.INTERNAL_SERVER_ERROR
        response.status = status.value
    }
}
