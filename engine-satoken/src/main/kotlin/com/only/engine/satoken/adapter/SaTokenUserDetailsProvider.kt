package com.only.engine.satoken.adapter

import com.only.engine.security.model.UserInfo

interface SaTokenUserDetailsProvider {

    fun loadUserByCredentials(username: String, password: String): UserInfo?

    fun loadUserById(loginId: Any): UserInfo?

    fun loadUserRoles(loginId: Any): Set<String>

    fun loadUserPermissions(loginId: Any): Set<String>
}