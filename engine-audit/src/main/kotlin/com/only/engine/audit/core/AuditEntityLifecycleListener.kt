package com.only.engine.audit.core

import com.only.engine.audit.config.properties.AuditProperties
import com.only.engine.spi.audit.AuditOperatorProvider
import org.hibernate.event.spi.PreInsertEvent
import org.hibernate.event.spi.PreInsertEventListener
import org.hibernate.event.spi.PreUpdateEvent
import org.hibernate.event.spi.PreUpdateEventListener
import java.lang.reflect.Field
import java.time.Instant

class AuditEntityLifecycleListener(
    private val properties: AuditProperties,
    private val auditOperatorProvider: AuditOperatorProvider,
    private val epochSecondProvider: () -> Long = { Instant.now().epochSecond },
) : PreInsertEventListener, PreUpdateEventListener {

    fun prePersist(entity: Any) {
        val operatorId = auditOperatorProvider.currentOperatorId()
        val operatorName = auditOperatorProvider.currentOperatorName()
        val epochSecond = epochSecondProvider()

        writeIfNull(entity, properties.createUserIdField, operatorId)
        writeIfNull(entity, properties.createByField, operatorName)
        writeIfNull(entity, properties.createTimeField, epochSecond)
        writeIfNull(entity, properties.updateUserIdField, operatorId)
        writeIfNull(entity, properties.updateByField, operatorName)
        writeIfNull(entity, properties.updateTimeField, epochSecond)
    }

    fun preUpdate(entity: Any) {
        val operatorId = auditOperatorProvider.currentOperatorId()
        val operatorName = auditOperatorProvider.currentOperatorName()
        val epochSecond = epochSecondProvider()

        writeValue(entity, properties.updateUserIdField, operatorId)
        writeValue(entity, properties.updateByField, operatorName)
        writeValue(entity, properties.updateTimeField, epochSecond)
    }

    override fun onPreInsert(event: PreInsertEvent): Boolean {
        prePersist(event.entity)
        syncState(event.state, event.persister.propertyNames, event.entity)
        return false
    }

    override fun onPreUpdate(event: PreUpdateEvent): Boolean {
        preUpdate(event.entity)
        syncState(event.state, event.persister.propertyNames, event.entity)
        return false
    }

    private fun writeIfNull(entity: Any, fieldName: String, value: Any?) {
        val field = findField(entity.javaClass, fieldName) ?: return
        field.isAccessible = true
        if (field.get(entity) == null) {
            setFieldValue(field, entity, value)
        }
    }

    private fun writeValue(entity: Any, fieldName: String, value: Any?) {
        val field = findField(entity.javaClass, fieldName) ?: return
        field.isAccessible = true
        setFieldValue(field, entity, value)
    }

    private fun setFieldValue(field: Field, entity: Any, value: Any?) {
        if (value == null && field.type.isPrimitive) {
            return
        }
        field.set(entity, value)
    }

    private fun syncState(state: Array<Any?>, propertyNames: Array<String>, entity: Any) {
        propertyNames.forEachIndexed { index, propertyName ->
            val field = findField(entity.javaClass, propertyName) ?: return@forEachIndexed
            field.isAccessible = true
            state[index] = field.get(entity)
        }
    }

    private fun findField(type: Class<*>, fieldName: String): Field? {
        var current: Class<*>? = type
        while (current != null && current != Any::class.java) {
            try {
                return current.getDeclaredField(fieldName)
            } catch (_: NoSuchFieldException) {
                current = current.superclass
            }
        }
        return null
    }
}
