package com.only.engine.translation.config

import com.only.engine.translation.translation.EpochSecondToDateStringTranslation
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
class BuiltinTranslationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(EpochSecondToDateStringTranslation::class)
    fun epochSecondToDateStringTranslation(): EpochSecondToDateStringTranslation =
        EpochSecondToDateStringTranslation()
}
