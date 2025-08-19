package com.only.engine.web.adivce

import com.only.engine.annotation.RespStatus
import com.only.engine.enums.HttpStatus
import com.only.engine.web.WebInitPrinter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.RestControllerAdvice
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

//    @ExceptionHandler(HttpClientErrorException::class, HttpServerErrorException::class)
//    fun restTemplateException(ex: Exception, request: HttpServletRequest): YmResult<Void> {
//        logError(request, ex.message ?: "", ex)
//        return YmResult.error(ex.message ?: "unknown error")
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException::class)
//    fun validationMethodArgumentException(ex: MethodArgumentNotValidException, request: HttpServletRequest) =
//        validationBindException(ex, request)
//
//    @ExceptionHandler(BindException::class)
//    fun validationBindException(ex: BindException, request: HttpServletRequest): YmResult<Void> {
//        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}：${it.defaultMessage}" }
//        val msg = "request parameter is invalid: $errors"
//        logInfo(request, msg, ex)
//        return YmResult.error(YmStandardCode.USER_SIDE_REQUEST_PARAMETER_EXCEPTION, msg)
//    }
//
//    @ExceptionHandler(HttpMessageNotReadableException::class)
//    fun httpMessageNotReadableException(
//        ex: HttpMessageNotReadableException,
//        request: HttpServletRequest,
//        response: HttpServletResponse
//    ): YmResult<Void> {
//        logWarning(request, ex.message ?: "", ex)
//        return YmResult.error(YmOldResultCode.USER_SIDE_REQUEST_PARAMETER_FORMAT_NOT_MATCH)
//    }
//
//    @ExceptionHandler(YmBizDataException::class)
//    fun ymBizDataException(ex: YmBizDataException, request: HttpServletRequest, response: HttpServletResponse): YmResult<*> {
//        logInfo(request, ex.message ?: "", ex)
//        handlerResponseStatus(ex, response)
//        return YmResult.error(ex.code, ex.message ?: "", ex.data)
//    }
//
//    @ExceptionHandler(YmBizException::class)
//    fun ymBizException(ex: YmBizException, request: HttpServletRequest, response: HttpServletResponse): YmResult<Void> {
//        logWarning(request, ex.message ?: "", ex)
//        handlerResponseStatus(ex, response)
//        return YmResult.error(ex.code, ex.message ?: "")
//    }
//
//    @ExceptionHandler(YmException::class)
//    fun ymException(ex: YmException, request: HttpServletRequest, response: HttpServletResponse): YmResult<Void> {
//        logError(request, ex.message ?: "", ex)
//        handlerResponseStatus(ex, response)
//        return YmResult.error(ex.code, ex.message ?: "")
//    }
//
//    @ExceptionHandler(FeignException::class)
//    fun feignException(ex: FeignException, request: HttpServletRequest): YmResult<Void> {
//        val code = ex.status().toString()
//        return when (code.first()) {
//            '4' -> YmResult.error(ex.status(), ex.message ?: "")
//            '5', '6' -> {
//                logWarning(request, ex.message ?: "", ex)
//                YmResult.error(ex.status(), ex.message ?: "")
//            }
//            else -> {
//                logError(request, ex.message ?: "", ex)
//                YmResult.error(YmStandardCode.SYSTEM_EXCEPTION, ex.message ?: "")
//            }
//        }
//    }
//
//    @ExceptionHandler(Exception::class)
//    fun exception(ex: Exception, request: HttpServletRequest): YmResult<Void> {
//        logError(request, ex.message ?: "", ex)
//        return YmResult.error(YmStandardCode.SYSTEM_EXCEPTION, ex.message ?: "")
//    }

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
