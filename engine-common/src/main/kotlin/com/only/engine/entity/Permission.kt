package com.only.engine.entity

/**
 * 权限信息
 *
 * @author LD_moxeii
 */
data class Permission(
    /**
     * 权限ID
     */
    val id: Long? = null,

    /**
     * 权限代码
     */
    val code: String? = null,

    /**
     * 权限名称
     */
    val name: String? = null,
)
