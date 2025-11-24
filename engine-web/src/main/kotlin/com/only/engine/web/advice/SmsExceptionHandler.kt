package com.only.engine.web.advice

import com.only.engine.entity.Result
import jakarta.servlet.http.HttpServletRequest
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
@ConditionalOnClass(SmsBlendException::class)
class SmsExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(SmsExceptionHandler::class.java)
    }

    /**
     * sms4j 发送异常
     */
    @ExceptionHandler(SmsBlendException::class)
    fun handleSmsBlendException(e: SmsBlendException, request: HttpServletRequest): Result<Unit> {
        val requestURI = request.requestURI
        log.error("请求地址'{}',发生 sms 短信异常.", requestURI, e)
        return Result.error(50000, "短信发送失败，请稍后再试...")
    }
}
