package com.only.engine.entity

import com.only.engine.exception.KnownException

data class UserInfo(
    val id: Any,
    val userType: String,
    val username: String,
    val roles: List<String> = emptyList(),
    val permissions: List<String> = emptyList(),
    val extra: Map<String, Any> = emptyMap(),
) {
    fun getLoginId(): String {
        requireNotNull(userType) { KnownException("用户类型不能为空") }
        requireNotNull(id) { KnownException("用户ID不能为空") }
        return "$userType:$id"
    }
}
