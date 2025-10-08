package com.only.engine.satoken.enums

import com.only.engine.exception.KnownException

enum class UserType(
    val userType: String,
) {
    UNKNOWN("未知"),

    SYS_USER("系统管理员"),

    ;

    companion object {
        private val enumMap: Map<String, UserType> by lazy {
            UserType.entries.associateBy { it.userType }
        }

        fun valueOf(value: String): UserType {
            return enumMap[value] ?: throw KnownException("枚举类型UserType枚举值转换异常，不存在的值: $value")
        }

        fun valueOfOrNull(value: String?): UserType? {
            return if (value == null) null else valueOf(value)
        }
    }

}
