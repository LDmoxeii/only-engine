package com.only.engine.web

import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.only.engine.web.config.I18nConfiguration
import com.only.engine.web.config.WebAdviceConfiguration
import com.only.engine.web.config.WebFilterConfiguration
import com.only.engine.web.misc.WebMessageConverterUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

@EnableConfigurationProperties(WebProperties::class)
@Import(
    I18nConfiguration::class,
    WebFilterConfiguration::class,
    WebAdviceConfiguration::class
)
@ConditionalOnProperty(prefix = "only.web", name = ["enable"], havingValue = "true", matchIfMissing = true)
class WebAutoConfiguration : WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(WebAutoConfiguration::class.java)
    }

    @Bean
    @ConditionalOnMissingBean
    fun mappingJackson2HttpMessageConverter(): MappingJackson2HttpMessageConverter =
        MappingJackson2HttpMessageConverter().apply {
            objectMapper = WebMessageConverterUtils.OBJECT_MAPPER
            printInit(MappingJackson2HttpMessageConverter::class.java, log)
        }
}
