package com.only.engine.translation.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.only.engine.translation.annotation.TranslationType
import com.only.engine.translation.core.TranslationInterface
import com.only.engine.translation.core.handler.TranslationBeanSerializerModifier
import com.only.engine.translation.core.handler.TranslationRegistry
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@AutoConfiguration
@Configuration
@EnableConfigurationProperties(TranslationBatchProperties::class)
class TranslationAutoConfiguration(
    private val objectMapper: ObjectMapper,
    private val providers: ObjectProvider<List<TranslationInterface<*>>>,
    private val batchProps: TranslationBatchProperties,
) {

    @PostConstruct
    fun init() {
        val map = mutableMapOf<String, TranslationInterface<*>>()
        providers.ifAvailable { list ->
            list.forEach { impl ->
                val ann = impl::class.java.getAnnotation(TranslationType::class.java)
                if (ann != null) {
                    map[ann.type] = impl
                }
            }
        }
        TranslationRegistry.TRANSLATION_MAPPER.putAll(map)

        objectMapper.setSerializerFactory(
            objectMapper.serializerFactory.withSerializerModifier(
                TranslationBeanSerializerModifier(
                    batchEnabled = batchProps.enable,
                    threshold = batchProps.threshold,
                    cacheEnabled = batchProps.cacheEnabled,
                    maxKeysPerGroup = batchProps.maxKeysPerGroup,
                )
            )
        )
    }
}
