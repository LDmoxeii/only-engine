package com.only.engine.printer

import org.slf4j.Logger

interface InitPrinter {
    fun moduleName(): String

    fun printInit(beanClass: Class<*>, logger: Logger) = logger.info("[{}]: init {}", moduleName(), beanClass.getName())

    fun printInit(bean: String, logger: Logger) = logger.info("[{}]: init {}", moduleName(), bean)
}
