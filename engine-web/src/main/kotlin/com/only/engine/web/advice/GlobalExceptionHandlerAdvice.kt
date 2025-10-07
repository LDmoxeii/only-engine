package com.only.engine.web.advice

import com.only.engine.annotation.RespStatus
import com.only.engine.constants.StandardCode
import com.only.engine.entity.Result
import com.only.engine.enums.HttpStatus
import com.only.engine.exception.KnownException
import com.only.engine.exception.WarnException
import com.only.engine.web.WebInitPrinter
import com.only.engine.web.config.properties.WebProperties
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.ConstraintViolationException
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

    /**
     * 找不到路由
     */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(ex: NoHandlerFoundException, request: HttpServletRequest): Result<Void> {
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
    ): Result<Void> {
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
    ): Result<Void> {
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
    ): Result<Void> {
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
    ): Result<Void> {
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
    ): Result<Void> {
        val message = "缺少必要的 Header 参数: ${ex.headerName}"
        logWarning(request, message, ex)
        return Result.error(StandardCode.UserSide.REQUEST_PARAMETER_EXCEPTION, message)
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

    /**
     * Servlet 异常
     */
    @ExceptionHandler(ServletException::class)
    fun handleServletException(ex: ServletException, request: HttpServletRequest): Result<Void> {
        val requestURI = request.requestURI
        logError(request, "请求地址'$requestURI',发生未知异常", ex)
        return Result.error(StandardCode.SystemSide.Exception)
    }

    /**
     * IO 异常处理
     * 特殊处理 SSE 连接中断
     */
    @ExceptionHandler(IOException::class)
    fun handleIOException(ex: IOException, request: HttpServletRequest): Result<Void>? {
        val requestURI = request.requestURI
        // SSE 经常性连接中断，例如关闭浏览器，直接屏蔽
        if (requestURI.contains("sse", ignoreCase = true)) {
            return null
        }
        logError(request, "请求地址'$requestURI',连接中断", ex)
        return Result.error(StandardCode.SystemSide.Exception)
    }

    /**
     * 运行时异常
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException, request: HttpServletRequest): Result<Void> {
        val requestURI = request.requestURI
        logError(request, "请求地址'$requestURI',发生运行时异常", ex)
        return Result.error(StandardCode.SystemSide.Exception)
    }

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
