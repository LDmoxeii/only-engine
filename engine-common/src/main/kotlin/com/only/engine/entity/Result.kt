package com.only.engine.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.only.engine.constants.StandardCode
import com.only.engine.constants.StrConstants
import com.only.engine.enums.BaseCode
import com.only.engine.enums.ResultCode

/**
 * 标准API响应结果包装器
 * 支持函数式编程风格，同时保持JVM兼容性
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Result<T>(
    override val code: Int = ResultCode.SUCCESS.code,
    override var message: String = ResultCode.SUCCESS.message,
    val data: T? = null,
    val timestamp: Long = System.currentTimeMillis(),
) : BaseCode {

    @JvmOverloads
    constructor(baseCode: BaseCode, data: T? = null) : this(baseCode.code, baseCode.message, data)

    constructor(standardCode: StandardCode, data: T? = null) : this(standardCode.code, standardCode.message, data)

    override val name: String
        get() = StrConstants.EMPTY

    companion object {

        @JvmStatic
        fun <T> ok(data: T? = null): Result<T> =
            Result(ResultCode.SUCCESS, data)

        @JvmStatic
        fun error(code: Int = ResultCode.BASE_ERROR.code, message: String): Result<Unit> =
            Result(code, message, null)

        @JvmStatic
        fun <T> error(standardCode: StandardCode): Result<T> =
            Result(standardCode.code, standardCode.message, null)

    }
}

