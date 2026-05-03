package com.only.engine.audit

import com.only.engine.printer.InitPrinter

interface AuditInitPrinter : InitPrinter {
    override fun moduleName(): String = "ENGINE_AUDIT"
}
