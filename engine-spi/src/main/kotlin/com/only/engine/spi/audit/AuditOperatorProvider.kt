package com.only.engine.spi.audit

interface AuditOperatorProvider {
    fun currentOperatorId(): Any?
    fun currentOperatorName(): String?
}
