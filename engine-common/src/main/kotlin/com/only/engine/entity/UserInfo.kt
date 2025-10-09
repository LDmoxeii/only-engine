package com.only.engine.entity

data class UserInfo(
    val id: Any,
    val userType: Int,
    val username: String,
    val roles: List<String> = emptyList(),
    val permissions: List<String> = emptyList(),
    val extra: Map<String, Any> = emptyMap(),
) {
    fun getLoginId(): String {
        return "$userType:$id"
    }
}
