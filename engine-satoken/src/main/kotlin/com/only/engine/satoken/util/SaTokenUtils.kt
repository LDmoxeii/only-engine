package com.only.engine.satoken.util

import cn.dev33.satoken.stp.StpUtil
import com.only.engine.security.util.SecurityUtils

object SaTokenUtils {

    @JvmStatic
    fun login(loginId: Any, deviceType: String? = null): String {
        return SecurityUtils.login(loginId, deviceType)
    }

    @JvmStatic
    fun logout() {
        SecurityUtils.logout()
    }

    @JvmStatic
    fun isLogin(): Boolean {
        return SecurityUtils.isLogin()
    }

    @JvmStatic
    fun checkLogin() {
        SecurityUtils.checkLogin()
    }

    @JvmStatic
    fun getLoginId(): Any? {
        return SecurityUtils.getLoginId()
    }

    @JvmStatic
    fun getCurrentUser() = SecurityUtils.getCurrentUser()

    @JvmStatic
    fun hasRole(role: String): Boolean {
        return SecurityUtils.hasRole(role)
    }

    @JvmStatic
    fun hasPermission(permission: String): Boolean {
        return SecurityUtils.hasPermission(permission)
    }

    @JvmStatic
    fun checkRole(role: String) {
        SecurityUtils.checkRole(role)
    }

    @JvmStatic
    fun checkPermission(permission: String) {
        SecurityUtils.checkPermission(permission)
    }

    @JvmStatic
    fun getTokenValue(): String? {
        return SecurityUtils.getTokenValue()
    }

    @JvmStatic
    fun getTokenTimeout(): Long {
        return SecurityUtils.getTokenTimeout()
    }

    @JvmStatic
    fun getSaTokenInfo(): Map<String, Any?> {
        return mapOf(
            "loginId" to StpUtil.getLoginIdDefaultNull(),
            "tokenValue" to StpUtil.getTokenValue(),
            "timeout" to StpUtil.getTokenTimeout(),
            "activeTimeout" to 0L,
            "sessionTimeout" to try {
                StpUtil.getSessionTimeout()
            } catch (e: Exception) {
                0L
            },
            "tokenName" to StpUtil.getTokenName()
        )
    }
}
