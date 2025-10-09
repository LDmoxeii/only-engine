package com.only.engine.redis.handler

import com.baomidou.lock.exception.LockFailureException
import com.only.engine.entity.Result
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Redis 异常处理器
 *
 * @author AprilWind
 */
@RestControllerAdvice
class RedisExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(RedisExceptionHandler::class.java)
    }

    /**
     * 分布式锁 Lock4j 异常
     */
    @ExceptionHandler(LockFailureException::class)
    fun handleLockFailureException(e: LockFailureException, request: HttpServletRequest): Result<Unit> {
        val requestURI = request.requestURI
        log.error("获取锁失败了'{}',发生 Lock4j 异常.", requestURI, e)
        return Result.error(503, "业务处理中,请稍后再试...")
    }
}
