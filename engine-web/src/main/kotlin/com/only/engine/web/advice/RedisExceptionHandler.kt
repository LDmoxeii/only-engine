package com.only.engine.web.advice

import com.baomidou.lock.exception.LockFailureException
import com.only.engine.error.CommonErrors
import com.only.engine.error.ErrorCategory
import com.only.engine.entity.Result
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Redis 异常处理器
 */
@RestControllerAdvice
@ConditionalOnClass(name = ["com.baomidou.lock.exception.LockFailureException"])
class RedisExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(RedisExceptionHandler::class.java)
    }

    /**
     * 分布式锁 Lock4j 异常
     */
    @ExceptionHandler(LockFailureException::class)
    fun handleLockFailureException(
        e: LockFailureException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = ExceptionResponseSupport.write(
        log = log,
        category = ErrorCategory.RATE_LIMIT,
        request = request,
        response = response,
        errorCode = CommonErrors.REQUEST_RATE_LIMITED,
        message = CommonErrors.REQUEST_RATE_LIMITED.message,
        ex = e,
    )
}
