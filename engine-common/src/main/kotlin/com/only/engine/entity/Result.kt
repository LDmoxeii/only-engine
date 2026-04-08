package com.only.engine.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.only.engine.constants.StandardCode
import com.only.engine.constants.StrConstants
import com.only.engine.enums.BaseCode
import com.only.engine.enums.ResultCode
import com.only.engine.error.ErrorCode
import com.only.engine.misc.ServletUtils
import com.only.engine.misc.ThreadLocalUtils

private fun resolveRequestPathOrNull(): String? =
    try {
        ServletUtils.getRequest()?.requestURI
    } catch (_: Throwable) {
        null
    }

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
    val requestId: String? = ThreadLocalUtils.getBizTrackCodeOrNull(),
    val path: String? = resolveRequestPathOrNull(),
) : BaseCode {

    @JvmOverloads
    constructor(baseCode: BaseCode, data: T? = null) : this(baseCode.code, baseCode.message, data)

    constructor(standardCode: StandardCode, data: T? = null) : this(standardCode.code, standardCode.message, data)

    override val name: String
        get() = StrConstants.EMPTY

    companion object {

        @JvmStatic
        fun <T> ok(data: T? = null): Result<T> =
            Result(ResultCode.SUCCESS.code, ResultCode.SUCCESS.message, data)

        @JvmStatic
        fun error(code: Int = ResultCode.BASE_ERROR.code, message: String): Result<Unit> =
            Result(code, message, null)

        @JvmStatic
        fun error(errorCode: ErrorCode, message: String = errorCode.message): Result<Unit> =
            Result(code = errorCode.code, message = message, data = null)

        @JvmStatic
        fun <T> error(standardCode: StandardCode): Result<T> =
            Result(standardCode.code, standardCode.message, null)

    }
}

