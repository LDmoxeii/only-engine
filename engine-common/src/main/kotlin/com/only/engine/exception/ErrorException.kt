package com.only.engine.exception

import com.only.engine.constants.StandardCode
import com.only.engine.enums.CodeEnum
import org.slf4j.event.Level

/**
 * 错误异常，用于系统级错误
 * 保持JVM兼容性，支持Java调用
 */
class ErrorException(
    override val code: Int = CodeEnum.FAIL.code,
    override val msg: String = CodeEnum.FAIL.name,
    cause: Throwable? = null
) : KnownException(code, msg, Level.ERROR, cause) {

    constructor(codeEnum: CodeEnum) : this(codeEnum.code, codeEnum.name)
    constructor(codeEnum: CodeEnum, cause: Throwable?) : this(codeEnum.code, codeEnum.name, cause)

    constructor(standardCode: StandardCode) : this(standardCode.code, standardCode.message)
    constructor(standardCode: StandardCode, cause: Throwable?) : this(standardCode.code, standardCode.message, cause)

    companion object {

        @JvmStatic
        fun systemError(): ErrorException =
            ErrorException(StandardCode.SystemSide.Exception)

        @JvmStatic
        fun systemError(message: String): ErrorException =
            ErrorException(StandardCode.SystemSide.EXCEPTION, message)

        @JvmStatic
        fun systemError(cause: Throwable): ErrorException =
            ErrorException(StandardCode.SystemSide.Exception, cause)

        @JvmStatic
        fun databaseError(): ErrorException =
            ErrorException(StandardCode.ThirdParty.DatabaseException)

        @JvmStatic
        fun databaseError(message: String): ErrorException =
            ErrorException(StandardCode.ThirdParty.DATABASE_EXCEPTION, message)
    }
}
