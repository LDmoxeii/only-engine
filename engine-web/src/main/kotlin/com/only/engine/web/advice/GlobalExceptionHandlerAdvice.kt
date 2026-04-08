package com.only.engine.web.advice

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonProcessingException
import com.only.engine.entity.Result
import com.only.engine.error.CommonErrors
import com.only.engine.exception.AppException
import com.only.engine.exception.DependencyException
import com.only.engine.exception.RequestException
import com.only.engine.exception.SystemException
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
import org.springframework.web.client.RestClientResponseException
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
    ): Result<Unit> = ExceptionResponseSupport.write(
        log = log,
        category = ex.errorCode.category,
        request = request,
        response = response,
        errorCode = ex.errorCode,
        message = ex.message,
        ex = ex,
    )

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

    @ExceptionHandler(RestClientResponseException::class)
    fun handleDependencyHttpError(
        ex: RestClientResponseException,
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
        SystemException(CommonErrors.SYSTEM_ERROR, resolveSafeSystemMessage(), cause = ex),
        request,
        response,
    )

    private fun resolveRequestMessage(ex: Exception): String =
        when (ex) {
            is MissingServletRequestParameterException -> "缺少必要的请求参数: ${ex.parameterName}"
            is MissingRequestHeaderException -> "缺少必要的请求头: ${ex.headerName}"
            is MissingPathVariableException -> "缺少必要的路径参数: ${ex.variableName}"
            is MethodArgumentTypeMismatchException -> "请求参数类型不匹配: ${ex.name}"
            is ConstraintViolationException -> ex.constraintViolations
                .asSequence()
                .map { it.message.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .joinToString("；")
                .ifBlank { "请求参数校验失败" }
            is BindException -> ex.bindingResult.allErrors
                .asSequence()
                .mapNotNull { it.defaultMessage?.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .joinToString("；")
                .ifBlank { "请求参数校验失败" }
            is HttpMessageNotReadableException, is JsonProcessingException, is JsonParseException -> "请求体格式错误"
            is HttpRequestMethodNotSupportedException -> "不支持的请求方法"
            is NoHandlerFoundException -> "请求地址不存在"
            is IllegalArgumentException -> CommonErrors.PARAM_INVALID.message
            else -> CommonErrors.PARAM_INVALID.message
        }

    private fun resolveSafeSystemMessage(): String = CommonErrors.SYSTEM_ERROR.message
}
