package com.only.engine.exception

import com.only.engine.constants.StandardCode
import com.only.engine.enums.CodeEnum
import org.slf4j.event.Level

/**
 * 警告异常，用于业务警告
 * 保持JVM兼容性，支持Java调用
 */
class WarnException @JvmOverloads constructor(
    code: Int = CodeEnum.FAIL.code,
    msg: String,
    cause: Throwable? = null,
) : KnownException(code, msg, Level.WARN, cause) {

    constructor(msg: String) : this(CodeEnum.FAIL.code, msg)
    constructor(codeEnum: CodeEnum) : this(codeEnum.code, codeEnum.name)
    constructor(codeEnum: CodeEnum, cause: Throwable?) : this(codeEnum.code, codeEnum.name, cause)
    
    // 使用StandardCode的构造函数
    constructor(standardCode: StandardCode) : this(standardCode.code, standardCode.message)
    constructor(standardCode: StandardCode, cause: Throwable?) : this(standardCode.code, standardCode.message, cause)
    
    companion object {
        
        @JvmStatic
        fun parameterInvalid(): WarnException =
            WarnException(StandardCode.UserSide.RequestParameterException)
            
        @JvmStatic
        fun parameterInvalid(paramName: String): WarnException =
            WarnException(StandardCode.UserSide.REQUEST_PARAMETER_REQUIRED_IS_NOT_NULL, "参数 '$paramName' 不能为空")
            
        @JvmStatic
        fun unauthorized(): WarnException =
            WarnException(StandardCode.UserSide.Unauthorized)
            
        @JvmStatic
        fun unauthorized(message: String): WarnException =
            WarnException(StandardCode.UserSide.UNAUTHORIZED, message)
    }
}
