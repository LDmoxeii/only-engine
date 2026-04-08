package com.only.engine.web.advice

import cn.dev33.satoken.exception.NotLoginException
import cn.dev33.satoken.exception.NotPermissionException
import cn.dev33.satoken.exception.NotRoleException
import com.only.engine.error.AuthErrors
import com.only.engine.error.ErrorCategory
import com.only.engine.entity.Result
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@ConditionalOnClass(name = ["cn.dev33.satoken.exception.NotLoginException"])
@ConditionalOnProperty(prefix = "only.engine.sa-token", name = ["enable"], havingValue = "true")
class SaTokenExceptionHandlerAdvice {

    companion object {
        private val log = LoggerFactory.getLogger(SaTokenExceptionHandlerAdvice::class.java)
    }

    /**
     * 权限码异常
     */
    @ExceptionHandler(NotPermissionException::class, NotRoleException::class)
    fun handleForbiddenException(
        e: RuntimeException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = ExceptionResponseSupport.write(
        log = log,
        category = ErrorCategory.AUTHORIZATION,
        request = request,
        response = response,
        errorCode = AuthErrors.ACCESS_DENIED,
        message = AuthErrors.ACCESS_DENIED.message,
        ex = e,
    )

    /**
     * 认证失败
     */
    @ExceptionHandler(NotLoginException::class)
    fun handleNotLoginException(
        e: NotLoginException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = ExceptionResponseSupport.write(
        log = log,
        category = ErrorCategory.AUTHENTICATION,
        request = request,
        response = response,
        errorCode = AuthErrors.LOGIN_REQUIRED,
        message = AuthErrors.LOGIN_REQUIRED.message,
        ex = e,
    )
}
