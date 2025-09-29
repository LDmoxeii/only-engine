package com.only.engine.satoken.handler

import cn.dev33.satoken.exception.NotLoginException
import cn.dev33.satoken.exception.NotPermissionException
import cn.dev33.satoken.exception.NotRoleException
import com.only.engine.entity.Result
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(-1) // 优先处理Sa-Token异常
@ConditionalOnClass(NotLoginException::class)
class SaTokenExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(SaTokenExceptionHandler::class.java)
    }

    /**
     * 权限码异常
     */
    @ExceptionHandler(NotPermissionException::class)
    fun handleNotPermissionException(e: NotPermissionException, request: HttpServletRequest): Result<Void> {
        val requestURI = request.requestURI
        log.error("请求地址 '$requestURI', 权限码校验失败 '${e.message}'")
        return Result.error(403, "没有访问权限，请联系管理员授权")
    }

    /**
     * 角色权限异常
     */
    @ExceptionHandler(NotRoleException::class)
    fun handleNotRoleException(e: NotRoleException, request: HttpServletRequest): Result<Void> {
        val requestURI = request.requestURI
        log.error("请求地址 '$requestURI', 角色权限校验失败 '${e.message}'")
        return Result.error(403, "没有访问权限，请联系管理员授权")
    }

    /**
     * 认证失败
     */
    @ExceptionHandler(NotLoginException::class)
    fun handleNotLoginException(e: NotLoginException, request: HttpServletRequest): Result<Void> {
        val requestURI = request.requestURI
        log.error("请求地址 '$requestURI', 认证失败 '${e.message}'")
        return Result.error(401, "认证失败，无法访问系统资源")
    }
}