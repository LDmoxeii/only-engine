package com.only.engine.spi.audit

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AuditOperatorProviderContractTest {

    @Test
    fun `provider contract allows nullable id and name`() {
        val provider = object : AuditOperatorProvider {
            override fun currentOperatorId(): Any? = null
            override fun currentOperatorName(): String? = null
        }
        assertNull(provider.currentOperatorId())
        assertNull(provider.currentOperatorName())
    }
}
