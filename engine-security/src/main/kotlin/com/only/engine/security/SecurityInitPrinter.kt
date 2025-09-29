package com.only.engine.security

import com.only.engine.printer.InitPrinter

interface SecurityInitPrinter : InitPrinter {

    override fun moduleName(): String = "ENGINE_SECURITY"
}
