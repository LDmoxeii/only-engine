package com.only.engine.annotation

import com.only.engine.enums.HttpStatus

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RespStatus(
    val value: HttpStatus = HttpStatus.OK
)
