package com.only.engine.translation

import com.only.engine.printer.InitPrinter

interface TranslationInitPrinter : InitPrinter {
    override fun moduleName(): String = "ENGINE_TRANSLATION"
}

