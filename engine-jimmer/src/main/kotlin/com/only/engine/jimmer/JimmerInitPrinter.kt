package com.only.engine.jimmer

import com.only.engine.printer.InitPrinter

interface JimmerInitPrinter : InitPrinter {

    override fun moduleName(): String = "ENGINE_JIMMER"
}
