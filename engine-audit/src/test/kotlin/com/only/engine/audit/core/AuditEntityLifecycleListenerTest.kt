package com.only.engine.audit.core

import com.only.engine.audit.config.properties.AuditProperties
import com.only.engine.spi.audit.AuditOperatorProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AuditEntityLifecycleListenerTest {

    private val properties = AuditProperties()

    @Test
    fun `prePersist fills create and update fields when null`() {
        val entity = AuditEntity()
        val listener = AuditEntityLifecycleListener(
            properties = properties,
            auditOperatorProvider = fixedProvider(1001L, "alice"),
            epochSecondProvider = { 1_700_000_000L },
        )

        listener.prePersist(entity)

        assertEquals(1001L, entity.createUserId)
        assertEquals("alice", entity.createBy)
        assertEquals(1_700_000_000L, entity.createTime)
        assertEquals(1001L, entity.updateUserId)
        assertEquals("alice", entity.updateBy)
        assertEquals(1_700_000_000L, entity.updateTime)
    }

    @Test
    fun `preUpdate overwrites update fields`() {
        val entity = AuditEntity(
            createUserId = 10L,
            createBy = "before",
            createTime = 100L,
            updateUserId = 11L,
            updateBy = "stale",
            updateTime = 101L,
        )
        val listener = AuditEntityLifecycleListener(
            properties = properties,
            auditOperatorProvider = fixedProvider(2002L, "bob"),
            epochSecondProvider = { 1_800_000_000L },
        )

        listener.preUpdate(entity)

        assertEquals(10L, entity.createUserId)
        assertEquals("before", entity.createBy)
        assertEquals(100L, entity.createTime)
        assertEquals(2002L, entity.updateUserId)
        assertEquals("bob", entity.updateBy)
        assertEquals(1_800_000_000L, entity.updateTime)
    }

    @Test
    fun `missing field is ignored`() {
        val entity = MissingAuditFieldsEntity(name = "demo")
        val listener = AuditEntityLifecycleListener(
            properties = properties,
            auditOperatorProvider = fixedProvider(3003L, "carol"),
            epochSecondProvider = { 1_900_000_000L },
        )

        listener.prePersist(entity)
        listener.preUpdate(entity)

        assertEquals("demo", entity.name)
    }

    @Test
    fun `null provider leaves nullable fields null`() {
        val entity = AuditEntity()
        val listener = AuditEntityLifecycleListener(
            properties = properties,
            auditOperatorProvider = fixedProvider(null, null),
            epochSecondProvider = { 2_000_000_000L },
        )

        listener.prePersist(entity)
        listener.preUpdate(entity)

        assertNull(entity.createUserId)
        assertNull(entity.createBy)
        assertEquals(2_000_000_000L, entity.createTime)
        assertNull(entity.updateUserId)
        assertNull(entity.updateBy)
        assertEquals(2_000_000_000L, entity.updateTime)
    }

    private fun fixedProvider(id: Any?, name: String?): AuditOperatorProvider = object : AuditOperatorProvider {
        override fun currentOperatorId(): Any? = id

        override fun currentOperatorName(): String? = name
    }

    private data class AuditEntity(
        var createUserId: Any? = null,
        var createBy: String? = null,
        var createTime: Long? = null,
        var updateUserId: Any? = null,
        var updateBy: String? = null,
        var updateTime: Long? = null,
    )

    private data class MissingAuditFieldsEntity(
        var name: String,
    )
}
