package com.only.engine.security.util

import com.only.engine.security.SecurityManager

object SecurityUtils {

    @JvmStatic
    fun login(loginId: Any, deviceType: String? = null): String {
        return SecurityManager.getInstance().tokenManager.createToken(loginId, deviceType)
    }

    @JvmStatic
    fun logout() {
        val tokenValue = SecurityManager.getInstance().tokenManager.getTokenValue()
        if (tokenValue != null) {
            SecurityManager.getInstance().authenticationService.logout(tokenValue)
        }
    }

    @JvmStatic
    fun isLogin(): Boolean {
        return SecurityManager.getInstance().authenticationService.isLogin()
    }

    @JvmStatic
    fun checkLogin() {
        if (!isLogin()) {
            throw com.only.engine.exception.ErrorException(401, "未登录")
        }
    }

    @JvmStatic
    fun getLoginId(): Any? {
        return SecurityManager.getInstance().authenticationService.getLoginId()
    }

    @JvmStatic
    fun getCurrentUser() = SecurityManager.getInstance().authenticationService.getCurrentUser()

    @JvmStatic
    fun hasRole(role: String): Boolean {
        return SecurityManager.getInstance().authorizationService.hasRole(role)
    }

    @JvmStatic
    fun hasPermission(permission: String): Boolean {
        return SecurityManager.getInstance().authorizationService.hasPermission(permission)
    }

    @JvmStatic
    fun checkRole(role: String) {
        SecurityManager.getInstance().authorizationService.checkRole(role)
    }

    @JvmStatic
    fun checkPermission(permission: String) {
        SecurityManager.getInstance().authorizationService.checkPermission(permission)
    }

    @JvmStatic
    fun getTokenValue(): String? {
        return SecurityManager.getInstance().tokenManager.getTokenValue()
    }

    @JvmStatic
    fun getTokenTimeout(): Long {
        val tokenValue = getTokenValue()
        return if (tokenValue != null) {
            SecurityManager.getInstance().tokenManager.getTokenTimeout(tokenValue)
        } else {
            0L
        }
    }
}