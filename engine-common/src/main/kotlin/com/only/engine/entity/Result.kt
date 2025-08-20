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

    /**
     * 是否成功
     */
    @get:JvmName("isSuccess")
    val success: Boolean
        get() = code == ResultCode.SUCCESS.code

    /**
     * 是否失败
     */
    @get:JvmName("isFailure")
    val failure: Boolean
        get() = !success

    companion object {

        @JvmStatic
        @JvmOverloads
        fun <T> ok(data: T? = null): Result<T> =
            Result(ResultCode.SUCCESS, data)

        @JvmStatic
        fun <T> success(data: T): Result<T> =
            Result(ResultCode.SUCCESS, data)

        @JvmStatic
        fun success(): Result<Void> =
            Result(ResultCode.SUCCESS, null)

        @JvmStatic
        @JvmOverloads
        fun error(code: Int = ResultCode.BASE_ERROR.code, message: String): Result<Void> =
            Result(code, message, null)

        @JvmStatic
        fun <T> error(standardCode: StandardCode): Result<T> =
            Result(standardCode.code, standardCode.message, null)

        @JvmStatic
        fun <T> error(standardCode: StandardCode, data: T?): Result<T> =
            Result(standardCode.code, standardCode.message, data)

        @JvmStatic
        fun <T> error(baseCode: BaseCode): Result<T> =
            Result(baseCode.code, baseCode.message, null)

        @JvmStatic
        fun <T> error(baseCode: BaseCode, data: T?): Result<T> =
            Result(baseCode.code, baseCode.message, data)

    }

    // ===================== Kotlin特性扩展 ===================== //

    /**
     * 映射数据类型，仅在成功时有效
     */
    inline fun <R> map(transform: (T?) -> R): Result<R> = when {
        success -> Result(code, message, transform(data), timestamp)
        else -> Result(code, message, null, timestamp)
    }

    /**
     * 平均映射，用于链式Result操作
     */
    inline fun <R> flatMap(transform: (T?) -> Result<R>): Result<R> = when {
        success -> transform(data)
        else -> Result(code, message, null, timestamp)
    }

    /**
     * 成功时执行操作
     */
    inline fun onSuccess(action: (T?) -> Unit): Result<T> {
        if (success) action(data)
        return this
    }

    /**
     * 失败时执行操作
     */
    inline fun onFailure(action: (code: Int, message: String) -> Unit): Result<T> {
        if (failure) action(code, message)
        return this
    }

    /**
     * 获取数据或默认值
     */
    fun getOrDefault(defaultValue: T): T = if (success) data ?: defaultValue else defaultValue

    /**
     * 获取数据或执行函数
     */
    inline fun getOrElse(onFailure: (code: Int, message: String) -> T): T =
        if (success) data ?: onFailure(code, message) else onFailure(code, message)

    /**
     * 获取数据或抛出异常
     */
    @Throws(RuntimeException::class)
    fun getOrThrow(): T? = when {
        success -> data
        else -> throw RuntimeException("请求失败: [$code] $message")
    }

    /**
     * 将结果转换为Kotlin的Result类型
     */
    fun toKotlinResult(): kotlin.Result<T?> = when {
        success -> kotlin.Result.success(data)
        else -> kotlin.Result.failure(RuntimeException("请求失败: [$code] $message"))
    }
}

