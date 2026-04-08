package com.only.engine.web.advice

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URLClassLoader

class OptionalDependencyIsolationTest {

    @Test
    fun `global exception handler advice should remain introspectable without optional integrations`() {
        assertClassIsIntrospectableWithoutOptionalDependencies("com.only.engine.web.advice.GlobalExceptionHandlerAdvice")
    }

    @Test
    fun `advice auto configuration should remain introspectable without optional integrations`() {
        assertClassIsIntrospectableWithoutOptionalDependencies("com.only.engine.web.config.AdviceAutoConfiguration")
    }

    private fun assertClassIsIntrospectableWithoutOptionalDependencies(className: String) {
        val urls = System.getProperty("java.class.path")
            .split(File.pathSeparator)
            .filter { it.isNotBlank() }
            .filterNot(::isOptionalIntegrationDependency)
            .map { File(it).toURI().toURL() }
            .toTypedArray()

        URLClassLoader(urls, ClassLoader.getPlatformClassLoader()).use { isolatedClassLoader ->
            val clazz = Class.forName(className, true, isolatedClassLoader)
            assertDoesNotThrow { clazz.declaredMethods }
        }
    }

    private fun isOptionalIntegrationDependency(path: String): Boolean {
        val normalized = path.replace('\\', '/').lowercase()
        return normalized.contains("lock4j") ||
            normalized.contains("satoken") ||
            normalized.contains("sa-token") ||
            normalized.contains("sms4j")
    }
}
