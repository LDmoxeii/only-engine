package com.only.engine.jimmer.config

import org.babyfish.jimmer.jackson.ImmutableModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

/**
 * Jimmer ObjectMapper Builder 定制器
 * 为 Jackson2ObjectMapperBuilder 添加 Jimmer 的 ImmutableModule
 */
class JimmerObjectMapperBuilderCustomizer : Jackson2ObjectMapperBuilderCustomizer {

    override fun customize(jacksonObjectMapperBuilder: Jackson2ObjectMapperBuilder) {
        jacksonObjectMapperBuilder.modules(ImmutableModule())
    }
}