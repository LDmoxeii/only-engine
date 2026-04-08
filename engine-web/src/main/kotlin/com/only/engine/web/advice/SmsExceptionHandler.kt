package com.only.engine.web.advice

import com.only.engine.error.CommonErrors
import com.only.engine.error.ErrorCategory
import com.only.engine.entity.Result
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.dromara.sms4j.comm.exception.SmsBlendException
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * SMS 异常处理器
 *
 * 统一将 sms4j 的异常封装为 Result 返回
 */
@RestControllerAdvice
@ConditionalOnClass(name = ["org.dromara.sms4j.comm.exception.SmsBlendException"])
class SmsExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(SmsExceptionHandler::class.java)
    }

    /**
     * sms4j 发送异常
     */
    @ExceptionHandler(SmsBlendException::class)
    fun handleSmsBlendException(
        e: SmsBlendException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = ExceptionResponseSupport.write(
        log = log,
        category = ErrorCategory.DEPENDENCY,
        request = request,
        response = response,
        errorCode = CommonErrors.DEPENDENCY_ERROR,
        message = "短信发送失败，请稍后再试...",
        ex = e,
    )
}
