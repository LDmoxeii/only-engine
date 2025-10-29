package com.only.engine.translation.core.handler

import com.only.engine.translation.core.TranslationInterface
import java.util.concurrent.ConcurrentHashMap

object TranslationRegistry {
    val TRANSLATION_MAPPER: MutableMap<String, TranslationInterface<*>> = ConcurrentHashMap()
}

