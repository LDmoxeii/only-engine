package com.only.engine.cap4k.addon.validation

import com.only4.cap4k.plugin.pipeline.api.ArtifactAddonContext
import com.only4.cap4k.plugin.pipeline.api.ArtifactAddonProvider
import com.only4.cap4k.plugin.pipeline.api.ArtifactLayoutConfig
import com.only4.cap4k.plugin.pipeline.api.ArtifactOutputKind
import com.only4.cap4k.plugin.pipeline.api.CanonicalModel
import com.only4.cap4k.plugin.pipeline.api.ConflictPolicy
import com.only4.cap4k.plugin.pipeline.api.ProjectConfig
import com.only4.cap4k.plugin.pipeline.api.ProjectLayout
import com.only4.cap4k.plugin.pipeline.api.StrongIdKind
import com.only4.cap4k.plugin.pipeline.api.StrongIdModel
import com.only4.cap4k.plugin.pipeline.api.TemplateConfig
import com.only4.cap4k.plugin.pipeline.api.TypeRegistryConfig
import com.only4.cap4k.plugin.pipeline.api.TypeRegistryEntry
import com.only4.cap4k.plugin.pipeline.api.TypeRegistryModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.util.ServiceLoader
import kotlin.io.path.writeText

class OnlyEngineValidatorAddonProviderTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `reads manifest from provider options and produces checked in application artifact`() {
        val provider = OnlyEngineValidatorAddonProvider()
        val manifestFile = writeManifest(
            """
            [
              {
                "package": "author",
                "name": "ValidAuthor",
                "desc": "Valid author id",
                "message": "author is invalid",
                "targets": ["FIELD", "VALUE_PARAMETER"],
                "valueType": "AuthorId",
                "parameters": [
                  { "name": "minimumLength", "type": "Int", "defaultValue": "3" }
                ]
              }
            ]
            """.trimIndent()
        )

        val items = provider.plan(
            context(
                options = mapOf("manifestFile" to manifestFile.toString()),
                model = CanonicalModel(
                    strongIds = listOf(
                        StrongIdModel(
                            typeName = "AuthorId",
                            packageName = "com.acme.demo.domain.aggregates.author",
                            kind = StrongIdKind.AGGREGATE_ROOT,
                        )
                    )
                ),
            )
        )

        assertEquals(1, items.size)
        val item = items.single()
        assertEquals("only-engine-validator", item.generatorId)
        assertEquals("application", item.moduleRole)
        assertEquals("addons/only-engine-validator/validator.kt.peb", item.templateId)
        assertEquals(ArtifactOutputKind.CHECKED_IN_SOURCE, item.outputKind)
        assertEquals("demo-application/src/main/kotlin", item.resolvedOutputRoot)
        assertEquals(
            "demo-application/src/main/kotlin/com/acme/demo/application/validators/author/ValidAuthor.kt",
            item.outputPath,
        )
        assertEquals(ConflictPolicy.OVERWRITE, item.conflictPolicy)
        assertEquals("com.acme.demo.application.validators.author", item.context["packageName"])
        assertEquals("ValidAuthor", item.context["annotationName"])
        assertEquals("AuthorId", item.context["valueType"])
        assertEquals(listOf("com.acme.demo.domain.aggregates.author.AuthorId"), item.context["imports"])
        assertEquals(
            listOf(mapOf("name" to "minimumLength", "type" to "Int", "defaultValue" to "3")),
            item.context["parameters"],
        )
    }

    @Test
    fun `missing manifest option fails with required message`() {
        val provider = OnlyEngineValidatorAddonProvider()

        val error = assertThrows(IllegalArgumentException::class.java) {
            provider.plan(context(options = emptyMap()))
        }

        assertEquals("only-engine-validator requires option manifestFile", error.message)
    }

    @Test
    fun `missing application module fails with required message`() {
        val provider = OnlyEngineValidatorAddonProvider()
        val manifestFile = writeManifest(validManifest())

        val error = assertThrows(IllegalArgumentException::class.java) {
            provider.plan(
                context(
                    options = mapOf("manifestFile" to manifestFile.toString()),
                    modules = emptyMap(),
                )
            )
        }

        assertEquals(
            "project.applicationModulePath is required when only-engine validator addon is installed.",
            error.message,
        )
    }

    @Test
    fun `service loader metadata registers both providers`() {
        val providers = ServiceLoader.load(ArtifactAddonProvider::class.java).toList()

        assertEquals(
            listOf("only-engine-enum-translation", "only-engine-validator"),
            providers.map { it.id }.sorted(),
        )
    }

    @Test
    fun `validator template resource is packaged under cap4k addon namespace`() {
        assertNotNull(
            javaClass.classLoader.getResource(
                "cap4k/addons/only-engine-validator/validator.kt.peb"
            )
        )
    }

    @Test
    fun `resolves type registry entry in parameter and value type`() {
        val provider = OnlyEngineValidatorAddonProvider()
        val manifestFile = writeManifest(
            """
            [
              {
                "package": "com.acme.demo.application.custom",
                "name": "ValidMoney",
                "message": "money is invalid",
                "targets": ["FIELD"],
                "valueType": "Money",
                "parameters": [
                  { "name": "currency", "type": "CurrencyCode", "defaultValue": "\"USD\"" }
                ]
              }
            ]
            """.trimIndent()
        )

        val item = provider.plan(
            context(
                options = mapOf("manifestFile" to manifestFile.toString()),
                model = CanonicalModel(
                    typeRegistry = TypeRegistryModel(
                        entries = mapOf("Money" to TypeRegistryEntry("com.acme.shared.Money"))
                    )
                ),
                typeRegistry = mapOf("CurrencyCode" to TypeRegistryEntry("com.acme.shared.CurrencyCode")),
            )
        ).single()

        assertEquals("com.acme.demo.application.custom", item.context["packageName"])
        assertEquals("demo-application/src/main/kotlin/com/acme/demo/application/custom/ValidMoney.kt", item.outputPath)
        assertEquals("Money", item.context["valueType"])
        assertEquals(
            listOf("com.acme.shared.CurrencyCode", "com.acme.shared.Money"),
            item.context["imports"],
        )
        assertEquals(
            listOf(mapOf("name" to "currency", "type" to "CurrencyCode", "defaultValue" to "\"USD\"")),
            item.context["parameters"],
        )
    }

    private fun writeManifest(content: String): Path {
        val path = tempDir.resolve("validators.json")
        path.writeText(content)
        return path
    }

    private fun validManifest(): String =
        """
        [
          {
            "package": "author",
            "name": "ValidAuthor",
            "message": "author is invalid",
            "targets": ["FIELD"],
            "valueType": "String"
          }
        ]
        """.trimIndent()

    private fun context(
        options: Map<String, Any?>,
        modules: Map<String, String> = mapOf("application" to "demo-application"),
        model: CanonicalModel = CanonicalModel(),
        typeRegistry: Map<String, TypeRegistryEntry> = emptyMap(),
    ): ArtifactAddonContext =
        ArtifactAddonContext(
            config = ProjectConfig(
                basePackage = "com.acme.demo",
                layout = ProjectLayout.MULTI_MODULE,
                modules = modules,
                typeRegistry = TypeRegistryConfig(entries = typeRegistry),
                sources = emptyMap(),
                generators = emptyMap(),
                templates = TemplateConfig("ddd-default", emptyList(), ConflictPolicy.OVERWRITE),
                artifactLayout = ArtifactLayoutConfig(),
            ),
            model = model,
            options = options,
        )
}
