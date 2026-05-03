package com.only.engine.satoken.config

import com.only.engine.spi.audit.AuditOperatorProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class SaTokenAuditProviderAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SaTokenAutoConfiguration::class.java))
        .withPropertyValues("only.engine.sa-token.enable=true")

    @Test
    fun `auto-configuration provides default audit operator provider`() {
        contextRunner.run { context ->
            assertThat(context).hasSingleBean(AuditOperatorProvider::class.java)
        }
    }
}
