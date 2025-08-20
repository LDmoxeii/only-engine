package com.only.engine.web.config

import com.only.engine.web.advice.*
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    ResponseAdvice::class,
    IgnoreResultWrapperResponseAdvice::class,
    GlobalExceptionHandlerAdvice::class,
    I18nResponseAdvice::class,
    StringResponseAdvice::class
)
class WebAdviceConfiguration
