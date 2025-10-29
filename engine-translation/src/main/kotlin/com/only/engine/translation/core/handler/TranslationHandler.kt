package com.only.engine.translation.core.handler

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.only.engine.translation.annotation.Translation
import com.only.engine.translation.core.TranslationContext
import com.only.engine.translation.core.TranslationInterface
import kotlin.reflect.full.memberProperties

class TranslationHandler(
    private val translation: Translation? = null
) : JsonSerializer<Any?>(), ContextualSerializer {

    override fun serialize(value: Any?, gen: JsonGenerator, serializers: SerializerProvider) {
        val ann = translation
        if (ann == null) {
            gen.writeObject(value)
            return
        }

        val impl: TranslationInterface<*>? = TranslationRegistry.TRANSLATION_MAPPER[ann.type]

        if (impl != null) {
            var src: Any? = value
            if (ann.mapper.isNotBlank()) {
                val bean = gen.currentValue
                src = readProperty(bean, ann.mapper)
            }
            if (src == null) {
                gen.writeNull()
                return
            }
            try {
                val cached = TranslationContext.get(ann.type, ann.other, src)
                val result = cached ?: impl.translation(src, ann.other).also {
                    TranslationContext.put(ann.type, ann.other, src, it)
                }
                gen.writeObject(result)
            } catch (e: Exception) {
                // 异常降级输出原值，保证健壮性
                gen.writeObject(src)
            }
        } else {
            gen.writeObject(value)
        }
    }

    override fun createContextual(prov: SerializerProvider, property: BeanProperty?): JsonSerializer<*> {
        if (property != null) {
            val ann = property.getAnnotation(Translation::class.java)
            if (ann != null) return TranslationHandler(ann)
            val ctxtAnn = property.getContextAnnotation(Translation::class.java)
            if (ctxtAnn is Translation) return TranslationHandler(ctxtAnn)
        }
        return prov.findValueSerializer(property?.type ?: prov.constructType(Any::class.java))
    }

    private fun readProperty(bean: Any?, name: String): Any? {
        if (bean == null) return null
        // Kotlin 反射读取属性
        return try {
            val kClass = bean::class
            val prop = kClass.memberProperties.firstOrNull { it.name == name }
            prop?.getter?.call(bean)
        } catch (_: Exception) {
            null
        }
    }
}
