package com.only.engine.web.adivce

import com.only.engine.annotation.RespStatus
import com.only.engine.constants.StandardCode
import com.only.engine.enums.HttpStatus
import com.only.engine.exception.KnownException
import com.only.engine.exception.WarnException
import com.only.engine.web.WebInitPrinter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.annotation.Order
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import kotlin.jvm.java

@Order(3)
@RestControllerAdvice
@ConditionalOnProperty(prefix = "yuanmeng.web", name = ["enable-exception-handler"], matchIfMissing = true)
class GlobalExceptionHandlerAdvice : WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandlerAdvice::class.java)
    }

    init {
        printInit(GlobalExceptionHandlerAdvice::class.java, log)
    }

    @ExceptionHandler(HttpClientErrorException::class, HttpServerErrorException::class)
    fun restTemplateException(ex: Exception, request: HttpServletRequest): com.only.engine.entity.Result<Void> {
        logError(request, ex.message ?: "", ex)
        return com.only.engine.entity.Result.error(message =  ex.message ?: "unknown error")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validationMethodArgumentException(ex: MethodArgumentNotValidException, request: HttpServletRequest) =
        validationBindException(ex, request)

    @ExceptionHandler(BindException::class)
    fun validationBindException(ex: BindException, request: HttpServletRequest): com.only.engine.entity.Result<Void> {
        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}：${it.defaultMessage}" }
        val msg = "request parameter is invalid: $errors"
        logInfo(request, msg, ex)
        return com.only.engine.entity.Result.error(StandardCode.UserSide.REQUEST_PARAMETER_EXCEPTION, msg)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun httpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): com.only.engine.entity.Result<Void> {
        logWarning(request, ex.message ?: "", ex)
        return com.only.engine.entity.Result.error(StandardCode.UserSide.REQUEST_PARAMETER_FORMAT_NOT_MATCH, "参数格式不匹配")
    }

    @ExceptionHandler(KnownException::class)
    fun KnowException(ex: KnownException, request: HttpServletRequest, response: HttpServletResponse): com.only.engine.entity.Result<*> {
        logInfo(request, ex.message ?: "", ex)
        handlerResponseStatus(ex, response)
        return com.only.engine.entity.Result.error(ex.code, ex.message ?: "")
    }

    @ExceptionHandler(WarnException::class)
    fun WarnException(ex: WarnException, request: HttpServletRequest, response: HttpServletResponse): com.only.engine.entity.Result<Void> {
        logError(request, ex.message ?: "", ex)
        handlerResponseStatus(ex, response)
        return com.only.engine.entity.Result.error(ex.code, ex.message ?: "")
    }

//    @ExceptionHandler(FeignException::class)
//    fun feignException(ex: FeignException, request: HttpServletRequest): com.only.engine.entity.Result<Void> {
//        val code = ex.status().toString()
//        return when (code.first()) {
//            '4' -> com.only.engine.entity.Result.error(ex.status(), ex.message ?: "")
//            '5', '6' -> {
//                logWarning(request, ex.message ?: "", ex)
//                com.only.engine.entity.Result.error(ex.status(), ex.message ?: "")
//            }
//            else -> {
//                logError(request, ex.message ?: "", ex)
//                com.only.engine.entity.Result.error(StandardCode.SYSTEM_EXCEPTION, ex.message ?: "")
//            }
//        }
//    }

    @ExceptionHandler(Exception::class)
    fun exception(ex: Exception, request: HttpServletRequest): com.only.engine.entity.Result<Void> {
        logError(request, ex.message ?: "", ex)
        return com.only.engine.entity.Result.error(StandardCode.SystemSide.EXCEPTION, ex.message ?: "")
    }

    /** 日志工具函数 */
    private fun logInfo(request: HttpServletRequest, message: String, e: Exception) =
        log.info("Path : [{}], Exception message: [{}], Exception: [{}]",
            request.requestURI, message, e::class.simpleName)

    private fun logWarning(request: HttpServletRequest, message: String, e: Exception) =
        log.warn("Path : [{}], Exception message: [{}], Exception: [{}]",
            request.requestURI, message, e::class.simpleName)

    private fun logError(request: HttpServletRequest, message: String, e: Exception) =
        log.error("Path : [{}], Exception message: [{}], Exception: [{}]",
            request.requestURI, message, e::class.simpleName, e)

    /** 响应状态处理 */
    private fun handlerResponseStatus(ex: Exception, response: HttpServletResponse, defStatus: HttpStatus? = null) {
        val respStatus = ex::class.java.getDeclaredAnnotation(RespStatus::class.java)
        val status = respStatus?.value ?: defStatus ?: HttpStatus.INTERNAL_SERVER_ERROR
        response.status = status.value
    }
}
