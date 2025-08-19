package com.only.engine.exception

import com.only.engine.enums.CodeEnum
import org.slf4j.event.Level

open class KnownException(
    val code: Int = CodeEnum.FAIL.code,
    val msg: String = CodeEnum.FAIL.name,
    val level: String = Level.DEBUG.toString(),
    cause: Throwable? = null,
) : RuntimeException(msg, cause) {

    constructor(codeEnum: CodeEnum) : this(codeEnum.code, codeEnum.name)
    constructor(codeEnum: CodeEnum, level: String) : this(codeEnum.code, codeEnum.name, level)
    constructor(codeEnum: CodeEnum, level: String, cause: Throwable?) : this(codeEnum.code, codeEnum.name, level, cause)

    constructor(msg: String) : this(CodeEnum.FAIL.code, msg)
    constructor(code: Int, msg: String) : this(code, msg, Level.DEBUG.toString())
    constructor(code: Int, msg: String, level: String) : this(code, msg, level, null)

    companion object {
        fun systemError(): KnownException =
            KnownException(CodeEnum.ERROR.code, CodeEnum.ERROR.name, Level.ERROR.toString())

        fun systemError(cause: Throwable): KnownException =
            KnownException(CodeEnum.ERROR.code, CodeEnum.ERROR.name, Level.ERROR.toString(), cause)

        fun illegalArgument(): KnownException =
            KnownException(CodeEnum.PARAM_INVALIDATE.code, CodeEnum.PARAM_INVALIDATE.name, Level.ERROR.toString())

        fun illegalArgument(argumentName: String): KnownException =
            KnownException(
                CodeEnum.PARAM_INVALIDATE.code,
                CodeEnum.PARAM_INVALIDATE.name,
                Level.ERROR.toString(),
                IllegalArgumentException(argumentName)
            )
    }
}
