package com.only.engine.security.entity

/**
 * 登录用户信息
 *
 * @author LD_moxeii
 */
data class LoginUser(
    /**
     * 用户ID
     */
    val id: Long? = null,

    /**
     * 用户名
     */
    val username: String? = null,

    /**
     * 用户权限列表
     */
    val permissions: List<Permission> = emptyList(),

    /**
     * 用户角色列表
     */
    val roles: List<Role> = emptyList(),
)
