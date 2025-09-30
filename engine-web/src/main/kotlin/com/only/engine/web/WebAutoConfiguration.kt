package com.only.engine.web

import com.only.engine.web.misc.WebMessageConverterUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

@AutoConfiguration
@EnableConfigurationProperties(WebProperties::class)
@ConditionalOnProperty(prefix = "only.web", name = ["enable"], havingValue = "true", matchIfMissing = true)
class WebAutoConfiguration : WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(WebAutoConfiguration::class.java)
    }

    @Bean
    @ConditionalOnMissingBean
    fun mappingJackson2HttpMessageConverter(
        builderCustomizers: List<Jackson2ObjectMapperBuilderCustomizer>,
    ): MappingJackson2HttpMessageConverter =
        MappingJackson2HttpMessageConverter().apply {
            objectMapper = WebMessageConverterUtils.createObjectMapper(builderCustomizers)
            WebMessageConverterUtils.OBJECT_MAPPER = objectMapper
            printInit(MappingJackson2HttpMessageConverter::class.java, log)
        }
}
