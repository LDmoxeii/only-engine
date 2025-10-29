package com.only.engine.json.validate

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * JSON 格式校验注解
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [JsonPatternValidator::class])
annotation class JsonPattern(
    /** 限制 JSON 类型，默认为 [JsonType.ANY]，即对象或数组都允许 */
    val type: JsonType = JsonType.ANY,

    /** 校验失败时的提示消息 */
    val message: String = "不是有效的 JSON 格式",

    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

