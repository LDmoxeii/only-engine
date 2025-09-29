package com.only.engine.security

import com.only.engine.security.model.PermissionRequest

interface AuthorizationService {

    fun hasRole(role: String): Boolean

    fun hasPermission(permission: String): Boolean

    fun hasAnyRole(vararg roles: String): Boolean

    fun hasAnyPermission(vararg permissions: String): Boolean

    fun checkRole(role: String)

    fun checkPermission(permission: String)

    fun checkAnyRole(vararg roles: String)

    fun checkAnyPermission(vararg permissions: String)

    fun validatePermission(request: PermissionRequest): Boolean
}