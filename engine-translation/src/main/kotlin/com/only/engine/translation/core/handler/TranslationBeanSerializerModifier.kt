package com.only.engine.translation.core.handler

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import com.fasterxml.jackson.databind.type.ArrayType
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.databind.type.MapType
import com.only.engine.translation.core.collector.PrecollectingArraySerializer
import com.only.engine.translation.core.collector.PrecollectingCollectionSerializer
import com.only.engine.translation.core.collector.PrecollectingMapSerializer

class TranslationBeanSerializerModifier(
    private val batchEnabled: Boolean = true,
    private val threshold: Int = 8,
    private val cacheEnabled: Boolean = true,
    private val maxKeysPerGroup: Int = 2000,
) : BeanSerializerModifier() {
    override fun changeProperties(
        config: SerializationConfig,
        beanDesc: BeanDescription,
        beanProperties: MutableList<BeanPropertyWriter>
    ): MutableList<BeanPropertyWriter> {
        beanProperties.forEach { writer ->
            val ser = writer.serializer
            if (ser is TranslationHandler) {
                writer.assignNullSerializer(ser)
            }
        }
        return beanProperties
    }

    override fun modifyCollectionSerializer(
        config: SerializationConfig,
        valueType: CollectionType,
        beanDesc: BeanDescription,
        serializer: JsonSerializer<*>?
    ): JsonSerializer<*>? {
        if (!batchEnabled || serializer == null) return serializer
        @Suppress("UNCHECKED_CAST")
        return PrecollectingCollectionSerializer(
            serializer as JsonSerializer<Any>,
            threshold,
            cacheEnabled,
            maxKeysPerGroup
        )
    }

    override fun modifyMapSerializer(
        config: SerializationConfig,
        valueType: MapType,
        beanDesc: BeanDescription,
        serializer: JsonSerializer<*>?
    ): JsonSerializer<*>? {
        if (!batchEnabled || serializer == null) return serializer
        @Suppress("UNCHECKED_CAST")
        return PrecollectingMapSerializer(
            serializer as JsonSerializer<Any>,
            threshold,
            cacheEnabled,
            maxKeysPerGroup
        )
    }

    override fun modifyArraySerializer(
        config: SerializationConfig,
        valueType: ArrayType,
        beanDesc: BeanDescription,
        serializer: JsonSerializer<*>?
    ): JsonSerializer<*>? {
        if (!batchEnabled || serializer == null) return serializer
        @Suppress("UNCHECKED_CAST")
        return PrecollectingArraySerializer(
            serializer as JsonSerializer<Any>,
            threshold,
            cacheEnabled,
            maxKeysPerGroup
        )
    }
}
