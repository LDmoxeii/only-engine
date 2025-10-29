package com.only.engine.translation.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class TranslationType(
    val type: String,
)

