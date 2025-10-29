package com.only.engine.translation.core

/**
 * Optional batch translation interface.
 * Implement alongside TranslationInterface to enable batch lookups.
 */
interface BatchTranslationInterface<T> {
    fun translationBatch(keys: Collection<Any>, other: String): Map<Any, T?>
}

