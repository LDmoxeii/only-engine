package com.only.engine.translation.config

import com.only.engine.translation.TranslationInitPrinter
import com.only.engine.translation.translation.AnyToJsonStringTranslation
import com.only.engine.translation.translation.EpochSecondToDateStringTranslation
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Auto-configures built-in translation implementations as Spring beans.
 * These beans will be discovered by TranslationAutoConfiguration and
 * registered into the TranslationRegistry.
 */
@AutoConfiguration
@Configuration
class BuiltinTranslationAutoConfiguration : TranslationInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(BuiltinTranslationAutoConfiguration::class.java)
    }

    @Bean
    @ConditionalOnMissingBean(EpochSecondToDateStringTranslation::class)
    fun epochSecondToDateStringTranslation(): EpochSecondToDateStringTranslation {
        printInit(EpochSecondToDateStringTranslation::class.java, log)
        return EpochSecondToDateStringTranslation()
    }

    @Bean
    @ConditionalOnMissingBean(AnyToJsonStringTranslation::class)
    fun anyToJsonStringTranslation(): AnyToJsonStringTranslation {
        printInit(AnyToJsonStringTranslation::class.java, log)
        return AnyToJsonStringTranslation()
    }
}
