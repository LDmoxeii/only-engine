package com.only.engine.entity

import com.only.engine.constants.StrConstants
import com.only.engine.enums.BaseCode
import com.only.engine.enums.ResultCode

data class Result<T>(
    override val code: Int = ResultCode.SUCCESS.code,
    override var message: String = ResultCode.SUCCESS.message,
    val data: T? = null,
    val timestamp: Long = System.currentTimeMillis(),
) : BaseCode {

    constructor(baseCode: BaseCode, data: T?) : this(baseCode.code, baseCode.message, data)

    override val name: String
        get() = StrConstants.EMPTY

    companion object {

        fun <T> ok(data: T? = null): Result<T> =
            Result(ResultCode.SUCCESS, data)

        fun error(code: Int = ResultCode.BASE_ERROR.code, message: String): Result<Void> =
            Result(code, message, null)

    }
}

