package com.only.engine.translation.core.handler

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier

class TranslationBeanSerializerModifier : BeanSerializerModifier() {
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
}

