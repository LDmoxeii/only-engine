package com.only.engine.satoken

import com.only.engine.printer.InitPrinter

interface SaTokenInitPrinter : InitPrinter {

    override fun moduleName(): String = "ENGINE_SATOKEN"
}
