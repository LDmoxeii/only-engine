package com.only.engine.exception

import com.only.engine.enums.CodeEnum
import org.slf4j.event.Level

class ErrorException @JvmOverloads constructor(
    code: Int = CodeEnum.FAIL.code,
    msg: String,
    cause: Throwable? = null,
) : KnownException(code, msg, Level.ERROR.toString(), cause) {

    constructor(msg: String) : this(CodeEnum.FAIL.code, msg)

    constructor(codeEnum: CodeEnum) : this(codeEnum.code, codeEnum.name)

    constructor(codeEnum: CodeEnum, cause: Throwable?) : this(codeEnum.code, codeEnum.name, cause)
}
