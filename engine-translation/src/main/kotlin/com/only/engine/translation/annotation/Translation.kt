package com.only.engine.translation.annotation

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.only.engine.translation.core.handler.TranslationHandler

@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER
)
@MustBeDocumented
@JacksonAnnotationsInside
@JsonSerialize(using = TranslationHandler::class)
annotation class Translation(
    /** 翻译类型标识，与实现上的 @TranslationType.type 对应 */
    val type: String,
    /** 可选：从哪个属性取源值，不填则使用当前字段值 */
    val mapper: String = "",
    /** 可选：附加参数（如字典类型等） */
    val other: String = "",
)

