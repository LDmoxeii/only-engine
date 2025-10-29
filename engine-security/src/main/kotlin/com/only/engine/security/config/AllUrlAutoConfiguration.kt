package com.only.engine.security.config

import com.only.engine.security.handler.AllUrlHandler
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class AllUrlAutoConfiguration {

    @Bean
    fun allUrlHandler(): AllUrlHandler = AllUrlHandler()
}

