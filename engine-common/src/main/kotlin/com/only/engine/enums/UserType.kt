package com.only.engine.enums

import com.only.engine.exception.KnownException

enum class UserType(
    val code: Int,
    val desc: String,
) {
    UNKNOWN(0, "未知"),

    SYS_USER(1, "系统管理员"),

    ;

    companion object {
        private val enumMap: Map<Int, UserType> by lazy {
            entries.associateBy { it.code }
        }

        fun valueOf(value: Int): UserType {
            return enumMap[value] ?: throw KnownException("枚举类型UserType枚举值转换异常，不存在的值: $value")
        }

        fun valueOfOrNull(value: Int?): UserType? {
            return if (value == null) null else valueOf(value)
        }
    }

}
