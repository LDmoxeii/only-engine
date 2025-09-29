package com.only.engine.satoken.adapter

import com.only.engine.security.model.UserInfo
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

@Component
@ConditionalOnMissingBean(SaTokenUserDetailsProvider::class)
class DefaultSaTokenUserDetailsProvider : SaTokenUserDetailsProvider {

    companion object {
        private val log = LoggerFactory.getLogger(DefaultSaTokenUserDetailsProvider::class.java)
    }

    override fun loadUserByCredentials(username: String, password: String): UserInfo? {
        log.warn("Using default user details provider - please implement SaTokenUserDetailsProvider for production use")
        // 默认实现，仅用于测试
        return if (username == "admin" && password == "admin") {
            UserInfo(
                id = 1,
                username = username,
                roles = setOf("ADMIN"),
                permissions = setOf("*:*")
            )
        } else {
            null
        }
    }

    override fun loadUserById(loginId: Any): UserInfo? {
        log.warn("Using default user details provider - please implement SaTokenUserDetailsProvider for production use")
        // 默认实现，仅用于测试
        return UserInfo(
            id = loginId,
            username = "user_$loginId",
            roles = setOf("USER"),
            permissions = setOf("read:*")
        )
    }

    override fun loadUserRoles(loginId: Any): Set<String> {
        log.warn("Using default user details provider - please implement SaTokenUserDetailsProvider for production use")
        return setOf("USER")
    }

    override fun loadUserPermissions(loginId: Any): Set<String> {
        log.warn("Using default user details provider - please implement SaTokenUserDetailsProvider for production use")
        return setOf("read:*")
    }
}