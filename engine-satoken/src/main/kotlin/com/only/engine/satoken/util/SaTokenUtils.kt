package com.only.engine.satoken.util

import com.only.engine.security.model.UserInfo
import com.only.engine.security.util.SecurityUtils

/**
 * SaToken工具类，提供与SecurityUtils的统一接口
 * 保持向后兼容性
 */
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
    fun getCurrentUser(): UserInfo? = SecurityUtils.getCurrentUser()

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
    fun getUsername(): String? {
        return getCurrentUser()?.username
    }

    @JvmStatic
    fun getUserId(): Any? {
        return getLoginId()
    }

    @JvmStatic
    fun getPermissions(): Set<String> {
        return getCurrentUser()?.permissions ?: emptySet()
    }

    @JvmStatic
    fun getRoles(): Set<String> {
        return getCurrentUser()?.roles ?: emptySet()
    }

    @JvmStatic
    fun getExtra(key: String): Any? {
        return getCurrentUser()?.extra?.get(key)
    }

    @JvmStatic
    fun logout(loginId: Any) {
        try {
            cn.dev33.satoken.stp.StpUtil.logout(loginId)
        } catch (e: Exception) {
            // 静默处理，因为这是兼容性方法
        }
    }

    /**
     * 获取Sa-Token相关信息汇总
     */
    @JvmStatic
    fun getSaTokenInfo(): Map<String, Any?> {
        val loginUser = getCurrentUser()
        return mapOf(
            "loginId" to getLoginId(),
            "username" to getUsername(),
            "tokenValue" to getTokenValue(),
            "timeout" to getTokenTimeout(),
            "roles" to getRoles(),
            "permissions" to getPermissions(),
            "userInfo" to loginUser
        )
    }
}
