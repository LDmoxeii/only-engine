package com.only.engine.satoken.adapter

import cn.dev33.satoken.stp.StpUtil
import com.only.engine.security.AuthorizationService
import com.only.engine.security.model.PermissionRequest
import org.slf4j.LoggerFactory

class SaTokenAuthorizationService(
    private val userDetailsProvider: SaTokenUserDetailsProvider,
) : AuthorizationService {

    companion object {
        private val log = LoggerFactory.getLogger(SaTokenAuthorizationService::class.java)
    }

    override fun hasRole(role: String): Boolean {
        return try {
            val loginId = StpUtil.getLoginIdDefaultNull() ?: return false
            val roles = userDetailsProvider.loadUserRoles(loginId)
            roles.contains(role)
        } catch (e: Exception) {
            log.warn("Failed to check role: {}", role, e)
            false
        }
    }

    override fun hasPermission(permission: String): Boolean {
        return try {
            val loginId = StpUtil.getLoginIdDefaultNull() ?: return false
            val permissions = userDetailsProvider.loadUserPermissions(loginId)
            permissions.contains(permission)
        } catch (e: Exception) {
            log.warn("Failed to check permission: {}", permission, e)
            false
        }
    }

    override fun hasAnyRole(vararg roles: String): Boolean {
        return try {
            val loginId = StpUtil.getLoginIdDefaultNull() ?: return false
            val userRoles = userDetailsProvider.loadUserRoles(loginId)
            roles.any { userRoles.contains(it) }
        } catch (e: Exception) {
            log.warn("Failed to check any role: {}", roles.contentToString(), e)
            false
        }
    }

    override fun hasAnyPermission(vararg permissions: String): Boolean {
        return try {
            val loginId = StpUtil.getLoginIdDefaultNull() ?: return false
            val userPermissions = userDetailsProvider.loadUserPermissions(loginId)
            permissions.any { userPermissions.contains(it) }
        } catch (e: Exception) {
            log.warn("Failed to check any permission: {}", permissions.contentToString(), e)
            false
        }
    }

    override fun checkRole(role: String) {
        if (!hasRole(role)) {
            throw com.only.engine.exception.ErrorException(403, "权限不足，需要角色: $role")
        }
    }

    override fun checkPermission(permission: String) {
        if (!hasPermission(permission)) {
            throw com.only.engine.exception.ErrorException(403, "权限不足，需要权限: $permission")
        }
    }

    override fun checkAnyRole(vararg roles: String) {
        if (!hasAnyRole(*roles)) {
            throw com.only.engine.exception.ErrorException(403, "权限不足，需要角色: ${roles.joinToString()}")
        }
    }

    override fun checkAnyPermission(vararg permissions: String) {
        if (!hasAnyPermission(*permissions)) {
            throw com.only.engine.exception.ErrorException(403, "权限不足，需要权限: ${permissions.joinToString()}")
        }
    }

    override fun validatePermission(request: PermissionRequest): Boolean {
        return hasPermission("${request.resource}:${request.action}")
    }
}
