package com.only.engine.translation.core.collector

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.only.engine.translation.core.BatchTranslationInterface
import com.only.engine.translation.core.TranslationContext
import com.only.engine.translation.core.handler.TranslationRegistry

class PrecollectingMapSerializer(
    private val delegate: JsonSerializer<Any>,
    private val threshold: Int = 8,
    private val cacheEnabled: Boolean = true,
    private val maxKeysPerGroup: Int = 2000,
) : JsonSerializer<Any>() {
    override fun serialize(value: Any?, gen: JsonGenerator, serializers: SerializerProvider) {
        val map = value as? Map<*, *> ?: run {
            delegate.serialize(value, gen, serializers)
            return
        }

        if (map.isEmpty() || map.size < threshold) {
            delegate.serialize(value, gen, serializers)
            return
        }

        try {
            TranslationContext.beginScope()
            val groups = mutableMapOf<Pair<String, String>, MutableSet<Any>>()
            var totalKeys = 0
            map.values.forEach { elem ->
                if (elem == null) return@forEach
                val metas = BeanIntrospectCache.metasOf(elem.javaClass)
                if (metas.isEmpty()) return@forEach
                metas.forEach { meta ->
                    val key = if (meta.mapper.isNotBlank()) BeanIntrospectCache.readProperty(
                        elem,
                        meta.mapper
                    ) else BeanIntrospectCache.readProperty(elem, meta.propertyName)
                    if (key != null) {
                        val grp = groups.getOrPut(meta.type to meta.other) { linkedSetOf() }
                        if (grp.size < maxKeysPerGroup) {
                            grp.add(key)
                            totalKeys++
                        }
                    }
                }
            }

            if (totalKeys > 0) {
                groups.forEach { (k, keys) ->
                    val impl = TranslationRegistry.TRANSLATION_MAPPER[k.first]
                    if (impl != null) {
                        val result: Map<Any, Any?> = if (impl is BatchTranslationInterface<*>) {
                            @Suppress("UNCHECKED_CAST")
                            (impl as BatchTranslationInterface<Any?>).translationBatch(keys, k.second)
                        } else {
                            keys.associateWith { src ->
                                runCatching { impl.translation(src, k.second) }.getOrNull()
                            }
                        }
                        if (cacheEnabled) TranslationContext.putAll(k.first, k.second, result)
                    }
                }
            }
        } finally {
            try {
                delegate.serialize(value, gen, serializers)
            } finally {
                TranslationContext.endScope()
            }
        }
    }
}

