package com.only.engine.translation.translation

import com.only.engine.json.misc.JsonUtils
import com.only.engine.translation.annotation.TranslationType
import com.only.engine.translation.core.BatchTranslationInterface
import com.only.engine.translation.core.TranslationInterface

/**
 * Translate any object to a JSON string using engine-json's JsonUtils.
 */
@TranslationType(type = AnyToJsonStringTranslation.TYPE)
class AnyToJsonStringTranslation : TranslationInterface<String>, BatchTranslationInterface<String> {

    companion object {
        const val TYPE = "any_to_json_str"
    }

    override fun translation(key: Any, other: String): String? = try {
        JsonUtils.toJsonString(key)
    } catch (_: Exception) {
        null
    }

    override fun translationBatch(keys: Collection<Any>, other: String): Map<Any, String?> {
        if (keys.isEmpty()) return emptyMap()
        return keys.associateWith { k ->
            try {
                JsonUtils.toJsonString(k)
            } catch (_: Exception) {
                null
            }
        }
    }
}

