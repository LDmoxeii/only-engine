package com.only.engine.satoken.config

import com.only.engine.satoken.provider.SaTokenAuditOperatorProvider
import com.only.engine.spi.audit.AuditOperatorProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class SaTokenAuditProviderAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SaTokenAutoConfiguration::class.java))
        .withPropertyValues("only.engine.sa-token.enable=true")

    @Test
    fun `auto-configuration provides default audit operator provider`() {
        contextRunner.run { context ->
            assertThat(context).hasSingleBean(AuditOperatorProvider::class.java)
            assertThat(context).hasSingleBean(SaTokenAuditOperatorProvider::class.java)
            assertThat(context.getBean(AuditOperatorProvider::class.java))
                .isInstanceOf(SaTokenAuditOperatorProvider::class.java)
        }
    }

    @Test
    fun `auto-configuration backs off when user provides audit operator provider`() {
        contextRunner
            .withUserConfiguration(CustomAuditOperatorProviderConfiguration::class.java)
            .run { context ->
                assertThat(context).hasSingleBean(AuditOperatorProvider::class.java)
                assertThat(context).doesNotHaveBean(SaTokenAuditOperatorProvider::class.java)
                assertThat(context.getBean(AuditOperatorProvider::class.java))
                    .isSameAs(context.getBean("customAuditOperatorProvider"))
            }
    }

    @Configuration(proxyBeanMethods = false)
    private class CustomAuditOperatorProviderConfiguration {
        @Bean
        fun customAuditOperatorProvider(): AuditOperatorProvider {
            return object : AuditOperatorProvider {
                override fun currentOperatorId(): Any? = 1L

                override fun currentOperatorName(): String? = "custom"
            }
        }
    }
}
