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

        // Sa-Token Session缓存键，与AuthenticationService保持一致
        private const val ROLES_KEY = "roles"
        private const val PERMISSIONS_KEY = "permissions"
        private const val USER_INFO_KEY = "userInfo"
    }

    override fun hasRole(role: String): Boolean {
        return try {
            val roles = getUserRoles()
            roles.contains(role)
        } catch (e: Exception) {
            log.warn("Failed to check role: {}", role, e)
            false
        }
    }

    override fun hasPermission(permission: String): Boolean {
        return try {
            val permissions = getUserPermissions()
            permissions.contains(permission)
        } catch (e: Exception) {
            log.warn("Failed to check permission: {}", permission, e)
            false
        }
    }

    override fun hasAnyRole(vararg roles: String): Boolean {
        return try {
            val userRoles = getUserRoles()
            roles.any { userRoles.contains(it) }
        } catch (e: Exception) {
            log.warn("Failed to check any role: {}", roles.contentToString(), e)
            false
        }
    }

    override fun hasAnyPermission(vararg permissions: String): Boolean {
        return try {
            val userPermissions = getUserPermissions()
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

    private fun getUserRoles(): Set<String> {
        val loginId = StpUtil.getLoginIdDefaultNull() ?: return emptySet()

        return try {
            // 优先从Sa-Token Session缓存获取
            val cachedRoles = StpUtil.getTokenSession().get(ROLES_KEY) as? Set<String>
            if (cachedRoles != null) {
                log.debug("Retrieved roles from Sa-Token cache for user: {}", loginId)
                return cachedRoles
            }

            // 缓存未命中，从数据源获取
            log.debug("Roles cache miss, loading from data source for user: {}", loginId)
            val roles = userDetailsProvider.loadUserRoles(loginId)

            // 缓存到Sa-Token Session
            StpUtil.getTokenSession().set(ROLES_KEY, roles)
            roles
        } catch (e: Exception) {
            log.warn("Failed to get user roles for: {}", loginId, e)
            emptySet()
        }
    }

    private fun getUserPermissions(): Set<String> {
        val loginId = StpUtil.getLoginIdDefaultNull() ?: return emptySet()

        return try {
            // 优先从Sa-Token Session缓存获取
            val cachedPermissions = StpUtil.getTokenSession().get(PERMISSIONS_KEY) as? Set<String>
            if (cachedPermissions != null) {
                log.debug("Retrieved permissions from Sa-Token cache for user: {}", loginId)
                return cachedPermissions
            }

            // 缓存未命中，从数据源获取
            log.debug("Permissions cache miss, loading from data source for user: {}", loginId)
            val permissions = userDetailsProvider.loadUserPermissions(loginId)

            // 缓存到Sa-Token Session
            StpUtil.getTokenSession().set(PERMISSIONS_KEY, permissions)
            permissions
        } catch (e: Exception) {
            log.warn("Failed to get user permissions for: {}", loginId, e)
            emptySet()
        }
    }
}
