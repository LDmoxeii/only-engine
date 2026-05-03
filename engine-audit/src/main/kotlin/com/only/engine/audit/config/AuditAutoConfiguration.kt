package com.only.engine.audit.config

import com.only.engine.audit.AuditInitPrinter
import com.only.engine.audit.config.properties.AuditProperties
import com.only.engine.audit.core.AuditEntityLifecycleListener
import com.only.engine.spi.audit.AuditOperatorProvider
import org.hibernate.integrator.spi.Integrator
import org.hibernate.jpa.boot.spi.IntegratorProvider
import org.hibernate.service.spi.SessionFactoryServiceRegistry
import org.hibernate.boot.Metadata
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.event.service.spi.EventListenerRegistry
import org.hibernate.event.spi.EventType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(name = ["org.hibernate.event.spi.PreInsertEventListener"])
@ConditionalOnProperty(prefix = "only.engine.audit", name = ["enable"], havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AuditProperties::class)
class AuditAutoConfiguration : AuditInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(AuditAutoConfiguration::class.java)
        private const val INTEGRATOR_PROVIDER_PROPERTY = "hibernate.integrator_provider"
    }

    @Bean
    fun auditEntityLifecycleListener(
        auditProperties: AuditProperties,
        auditOperatorProvider: ObjectProvider<AuditOperatorProvider>,
    ): AuditEntityLifecycleListener {
        printInit(AuditEntityLifecycleListener::class.java, log)
        return AuditEntityLifecycleListener(
            properties = auditProperties,
            auditOperatorProvider = auditOperatorProvider.getIfAvailable() ?: NullableAuditOperatorProvider
        )
    }

    @Bean
    fun auditHibernatePropertiesCustomizer(
        auditEntityLifecycleListener: AuditEntityLifecycleListener,
    ): HibernatePropertiesCustomizer {
        printInit("auditHibernatePropertiesCustomizer", log)

        return HibernatePropertiesCustomizer { hibernateProperties ->
            val auditIntegrator = AuditLifecycleIntegrator(auditEntityLifecycleListener)
            val existingProvider = hibernateProperties[INTEGRATOR_PROVIDER_PROPERTY] as? IntegratorProvider
            hibernateProperties[INTEGRATOR_PROVIDER_PROPERTY] = IntegratorProvider {
                val existingIntegrators = existingProvider?.integrators ?: emptyList()
                existingIntegrators + auditIntegrator
            }
        }
    }

    private class AuditLifecycleIntegrator(
        private val auditEntityLifecycleListener: AuditEntityLifecycleListener,
    ) : Integrator {

        override fun integrate(
            metadata: Metadata,
            bootstrapContext: org.hibernate.boot.spi.BootstrapContext,
            sessionFactory: SessionFactoryImplementor,
        ) {
            val registry = sessionFactory.serviceRegistry.getService(EventListenerRegistry::class.java)
                ?: return
            registry.appendListeners(EventType.PRE_INSERT, auditEntityLifecycleListener)
            registry.appendListeners(EventType.PRE_UPDATE, auditEntityLifecycleListener)
        }

        override fun disintegrate(
            sessionFactory: SessionFactoryImplementor,
            serviceRegistry: SessionFactoryServiceRegistry,
        ) = Unit
    }

    private object NullableAuditOperatorProvider : AuditOperatorProvider {
        override fun currentOperatorId(): Any? = null

        override fun currentOperatorName(): String? = null
    }
}
