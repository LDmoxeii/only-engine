package com.only.engine.json.validate

import com.only.engine.json.misc.JsonUtils
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/**
 * JSON 格式校验器
 */
class JsonPatternValidator : ConstraintValidator<JsonPattern, String> {

    private lateinit var jsonType: JsonType

    override fun initialize(annotation: JsonPattern) {
        this.jsonType = annotation.type
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value.isNullOrBlank()) {
            // 交给 @NotBlank 或 @NotNull 控制是否允许为空
            return true
        }
        return when (jsonType) {
            JsonType.ANY -> JsonUtils.isJson(value)
            JsonType.OBJECT -> JsonUtils.isJsonObject(value)
            JsonType.ARRAY -> JsonUtils.isJsonArray(value)
        }
    }
}

