package com.only.engine.exception

import com.only.engine.constants.StandardCode
import com.only.engine.enums.CodeEnum
import org.slf4j.event.Level

open class KnownException(
    open val code: Int = CodeEnum.FAIL.code,
    open val msg: String = CodeEnum.FAIL.name,
    val level: Level = Level.DEBUG,
    cause: Throwable? = null
) : RuntimeException(msg, cause) {

    constructor(codeEnum: CodeEnum) : this(codeEnum.code, codeEnum.name)
    constructor(codeEnum: CodeEnum, level: Level) : this(codeEnum.code, codeEnum.name, level)
    constructor(codeEnum: CodeEnum, level: Level, cause: Throwable?) : this(codeEnum.code, codeEnum.name, level, cause)
    constructor(msg: String) : this(CodeEnum.FAIL.code, msg)

    // 使用StandardCode的构造函数
    constructor(standardCode: StandardCode, cause: Throwable? = null) : this(standardCode.code, standardCode.message, Level.INFO, cause)

    companion object {

        @JvmStatic
        fun systemError(): KnownException =
            KnownException(StandardCode.SystemSide.Exception, null)

        @JvmStatic
        fun systemError(cause: Throwable): KnownException =
            KnownException(StandardCode.SystemSide.Exception, cause)

        @JvmStatic
        fun systemError(message: String): KnownException =
            KnownException(StandardCode.SystemSide.EXCEPTION, message, Level.ERROR)

        @JvmStatic
        fun illegalArgument(): KnownException =
            KnownException(StandardCode.UserSide.RequestParameterException)

        @JvmStatic
        fun illegalArgument(argumentName: String): KnownException =
            KnownException(
                StandardCode.UserSide.REQUEST_PARAMETER_REQUIRED_IS_NOT_NULL,
                "参数 '$argumentName' 不能为空",
                Level.WARN,
                IllegalArgumentException(argumentName)
            )

        @JvmStatic
        fun unauthorized(): KnownException =
            KnownException(StandardCode.UserSide.Unauthorized)

        @JvmStatic
        fun unauthorized(message: String): KnownException =
            KnownException(StandardCode.UserSide.UNAUTHORIZED, message, Level.WARN)
    }

    // Kotlin扩展函数风格的工厂方法
    fun withCause(cause: Throwable): KnownException =
        KnownException(code, msg, level, cause)

    fun withLevel(newLevel: Level): KnownException =
        KnownException(code, msg, newLevel, cause)
}
