package com.only.engine.json

import com.only.engine.printer.InitPrinter

interface JsonInitPrinter : InitPrinter {

    override fun moduleName(): String = "ENGINE_JSON"
}
