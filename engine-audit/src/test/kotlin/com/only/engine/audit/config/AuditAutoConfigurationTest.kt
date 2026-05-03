package com.only.engine.audit.config

import com.only.engine.audit.core.AuditEntityLifecycleListener
import com.only.engine.spi.audit.AuditOperatorProvider
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.integrator.spi.Integrator
import org.hibernate.jpa.boot.spi.IntegratorProvider
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class AuditAutoConfigurationTest {

    private val contextRunnerWithProvider = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AuditAutoConfiguration::class.java))
        .withUserConfiguration(TestProviderConfiguration::class.java)

    private val contextRunnerWithoutProvider = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AuditAutoConfiguration::class.java))

    @Test
    fun `auto-configuration creates expected beans when enabled`() {
        contextRunnerWithProvider
            .withPropertyValues("only.engine.audit.enable=true")
            .run { context ->
                assertThat(context).hasSingleBean(AuditAutoConfiguration::class.java)
                assertThat(context).hasSingleBean(AuditEntityLifecycleListener::class.java)
                assertThat(context).hasSingleBean(HibernatePropertiesCustomizer::class.java)
            }
    }

    @Test
    fun `disabled property prevents the auto-config beans`() {
        contextRunnerWithProvider
            .withPropertyValues("only.engine.audit.enable=false")
            .run { context ->
                assertThat(context).doesNotHaveBean(AuditAutoConfiguration::class.java)
                assertThat(context).doesNotHaveBean(AuditEntityLifecycleListener::class.java)
                assertThat(context).doesNotHaveBean(HibernatePropertiesCustomizer::class.java)
            }
    }

    @Test
    fun `auto-configuration starts without provider bean`() {
        contextRunnerWithoutProvider
            .withPropertyValues("only.engine.audit.enable=true")
            .run { context ->
                assertThat(context).hasSingleBean(AuditAutoConfiguration::class.java)
                assertThat(context).hasSingleBean(AuditEntityLifecycleListener::class.java)
                assertThat(context).hasSingleBean(HibernatePropertiesCustomizer::class.java)
            }
    }

    @Test
    fun `hibernate customizer preserves existing integrator provider`() {
        contextRunnerWithoutProvider
            .withPropertyValues("only.engine.audit.enable=true")
            .run { context ->
                val customizer = context.getBean(HibernatePropertiesCustomizer::class.java)
                val existingIntegrator = NoopIntegrator()
                val existingProvider = IntegratorProvider { listOf(existingIntegrator) }
                val hibernateProperties = mutableMapOf<String, Any>(
                    "hibernate.integrator_provider" to existingProvider
                )

                customizer.customize(hibernateProperties)

                val mergedProvider = hibernateProperties["hibernate.integrator_provider"] as IntegratorProvider
                val integrators = mergedProvider.integrators

                assertThat(integrators).hasSize(2)
                assertThat(integrators[0]).isSameAs(existingIntegrator)
                assertThat(integrators[1]).isInstanceOf(Integrator::class.java)
            }
    }

    @Configuration(proxyBeanMethods = false)
    private class TestProviderConfiguration {
        @Bean
        fun auditOperatorProvider(): AuditOperatorProvider = object : AuditOperatorProvider {
            override fun currentOperatorId(): Any? = 42L

            override fun currentOperatorName(): String? = "tester"
        }
    }

    private class NoopIntegrator : Integrator {
        override fun integrate(
            metadata: org.hibernate.boot.Metadata,
            bootstrapContext: org.hibernate.boot.spi.BootstrapContext,
            sessionFactory: org.hibernate.engine.spi.SessionFactoryImplementor,
        ) = Unit

        override fun disintegrate(
            sessionFactory: org.hibernate.engine.spi.SessionFactoryImplementor,
            serviceRegistry: org.hibernate.service.spi.SessionFactoryServiceRegistry,
        ) = Unit
    }
}
