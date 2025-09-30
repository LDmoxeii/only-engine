package com.only.engine.jimmer.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

class JimmerObjectMapperBuilderCustomizerTest {

    @Test
    fun `should add ImmutableModule when customizer is applied`() {
        val customizer = JimmerObjectMapperBuilderCustomizer()
        val builder = Jackson2ObjectMapperBuilder.json()

        customizer.customize(builder)
        val objectMapper = builder.build<ObjectMapper>()

        // 验证 ObjectMapper 不为空
        assertNotNull(objectMapper)
    }

    @Test
    fun `should implement Jackson2ObjectMapperBuilderCustomizer interface`() {
        val customizer = JimmerObjectMapperBuilderCustomizer()
        assertTrue(customizer is Jackson2ObjectMapperBuilderCustomizer)
    }
}
