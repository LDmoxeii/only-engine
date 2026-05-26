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
import org.junit.jupiter.api.Assertions.assertTrue
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
    fun `resolves type registry entry in value type while parameters use annotation scalar types`() {
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
                  { "name": "currency", "type": "String", "defaultValue": "\"USD\"" }
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
            )
        ).single()

        assertEquals("com.acme.demo.application.custom", item.context["packageName"])
        assertEquals("demo-application/src/main/kotlin/com/acme/demo/application/custom/ValidMoney.kt", item.outputPath)
        assertEquals("Money", item.context["valueType"])
        assertEquals(
            listOf("com.acme.shared.Money"),
            item.context["imports"],
        )
        assertEquals(
            listOf(mapOf("name" to "currency", "type" to "String", "defaultValue" to "\"USD\"")),
            item.context["parameters"],
        )
    }

    @Test
    fun `unsupported annotation parameter custom type fails fast with parameter location`() {
        val provider = OnlyEngineValidatorAddonProvider()
        val manifestFile = writeManifest(
            """
            [
              {
                "package": "author",
                "name": "ValidAuthor",
                "message": "author is invalid",
                "targets": ["FIELD"],
                "valueType": "String",
                "parameters": [
                  { "name": "authorId", "type": "AuthorId" }
                ]
              }
            ]
            """.trimIndent()
        )

        val error = assertThrows(IllegalArgumentException::class.java) {
            provider.plan(context(options = mapOf("manifestFile" to manifestFile.toString())))
        }

        assertEquals(
            "validator manifest entry[0].parameters[0].type AuthorId is not a supported annotation parameter type",
            error.message,
        )
    }

    @Test
    fun `ambiguous strong id simple name fails fast for value type`() {
        val provider = OnlyEngineValidatorAddonProvider()
        val manifestFile = writeManifest(validManifest(valueType = "AuthorId"))

        val error = assertThrows(IllegalArgumentException::class.java) {
            provider.plan(
                context(
                    options = mapOf("manifestFile" to manifestFile.toString()),
                    model = CanonicalModel(
                        strongIds = listOf(
                            StrongIdModel(
                                typeName = "AuthorId",
                                packageName = "com.acme.demo.domain.aggregates.author",
                                kind = StrongIdKind.AGGREGATE_ROOT,
                            ),
                            StrongIdModel(
                                typeName = "AuthorId",
                                packageName = "com.acme.demo.domain.aggregates.book",
                                kind = StrongIdKind.AGGREGATE_ROOT,
                            ),
                        )
                    ),
                )
            )
        }

        assertEquals("Ambiguous validator type reference: AuthorId", error.message)
    }

    @Test
    fun `invalid targets and identifiers fail before rendering`() {
        val provider = OnlyEngineValidatorAddonProvider()
        val invalidTarget = writeManifest(
            """
            [
              {
                "package": "author",
                "name": "ValidAuthor",
                "message": "author is invalid",
                "targets": ["BOGUS"],
                "valueType": "String"
              }
            ]
            """.trimIndent()
        )

        val targetError = assertThrows(IllegalArgumentException::class.java) {
            provider.plan(context(options = mapOf("manifestFile" to invalidTarget.toString())))
        }

        assertEquals("validator manifest entry[0].targets[0] BOGUS is not a supported AnnotationTarget", targetError.message)

        val invalidAnnotationName = writeManifest(
            """
            [
              {
                "package": "author",
                "name": "Valid-Author",
                "message": "author is invalid",
                "targets": ["FIELD"],
                "valueType": "String"
              }
            ]
            """.trimIndent()
        )

        val annotationNameError = assertThrows(IllegalArgumentException::class.java) {
            provider.plan(context(options = mapOf("manifestFile" to invalidAnnotationName.toString())))
        }

        assertEquals("validator manifest entry[0].name is not a valid Kotlin identifier", annotationNameError.message)

        val invalidIdentifier = writeManifest(
            """
            [
              {
                "package": "author.bad-name",
                "name": "ValidAuthor",
                "message": "author is invalid",
                "targets": ["FIELD"],
                "valueType": "String",
                "parameters": [
                  { "name": "min-length", "type": "Int" }
                ]
              }
            ]
            """.trimIndent()
        )

        val identifierError = assertThrows(IllegalArgumentException::class.java) {
            provider.plan(context(options = mapOf("manifestFile" to invalidIdentifier.toString())))
        }

        assertEquals("validator manifest entry[0].package segment bad-name is not a valid Kotlin identifier", identifierError.message)

        val invalidParameterName = writeManifest(
            """
            [
              {
                "package": "author",
                "name": "ValidAuthor",
                "message": "author is invalid",
                "targets": ["FIELD"],
                "valueType": "String",
                "parameters": [
                  { "name": "min-length", "type": "Int" }
                ]
              }
            ]
            """.trimIndent()
        )

        val parameterNameError = assertThrows(IllegalArgumentException::class.java) {
            provider.plan(context(options = mapOf("manifestFile" to invalidParameterName.toString())))
        }

        assertEquals(
            "validator manifest entry[0].parameters[0].name is not a valid Kotlin identifier",
            parameterNameError.message,
        )
    }

    @Test
    fun `package under base package is absolute and other package is relative`() {
        val provider = OnlyEngineValidatorAddonProvider()
        val underBasePackage = writeManifest(
            """
            [
              {
                "package": "com.acme.demo.application.custom",
                "name": "ValidAuthor",
                "message": "author is invalid",
                "targets": ["FIELD"],
                "valueType": "String"
              }
            ]
            """.trimIndent()
        )

        val absoluteItem = provider.plan(context(options = mapOf("manifestFile" to underBasePackage.toString()))).single()

        assertEquals("com.acme.demo.application.custom", absoluteItem.context["packageName"])

        val outsideBasePackage = writeManifest(
            """
            [
              {
                "package": "com.external.validators",
                "name": "ValidAuthor",
                "message": "author is invalid",
                "targets": ["FIELD"],
                "valueType": "String"
              }
            ]
            """.trimIndent()
        )

        val relativeItem = provider.plan(context(options = mapOf("manifestFile" to outsideBasePackage.toString()))).single()

        assertEquals(
            "com.acme.demo.application.validators.com.external.validators",
            relativeItem.context["packageName"],
        )
    }

    @Test
    fun `relative manifest file path fails fast`() {
        val provider = OnlyEngineValidatorAddonProvider()

        val error = assertThrows(IllegalArgumentException::class.java) {
            provider.plan(context(options = mapOf("manifestFile" to "validation/validators.json")))
        }

        assertEquals("only-engine-validator option manifestFile must be an absolute path", error.message)
    }

    @Test
    fun `default values are minimally validated for annotation scalar types`() {
        val provider = OnlyEngineValidatorAddonProvider()
        val manifestFile = writeManifest(
            """
            [
              {
                "package": "author",
                "name": "ValidAuthor",
                "message": "author is invalid",
                "targets": ["FIELD"],
                "valueType": "String",
                "parameters": [
                  { "name": "enabled", "type": "Boolean", "defaultValue": "yes" }
                ]
              }
            ]
            """.trimIndent()
        )

        val error = assertThrows(IllegalArgumentException::class.java) {
            provider.plan(context(options = mapOf("manifestFile" to manifestFile.toString())))
        }

        assertEquals(
            "validator manifest entry[0].parameters[0].defaultValue yes is not a valid Boolean literal",
            error.message,
        )
    }

    @Test
    fun `template and provider context keep annotation parameters separate from validator value type`() {
        val provider = OnlyEngineValidatorAddonProvider()
        val manifestFile = writeManifest(
            """
            [
              {
                "package": "author",
                "name": "ValidAuthor",
                "message": "author is invalid",
                "targets": ["FIELD"],
                "valueType": "AuthorId",
                "parameters": [
                  { "name": "minimumLength", "type": "Int", "defaultValue": "3" }
                ]
              }
            ]
            """.trimIndent()
        )

        val item = provider.plan(
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
        ).single()
        val template = javaClass.classLoader
            .getResourceAsStream("cap4k/addons/only-engine-validator/validator.kt.peb")!!
            .bufferedReader()
            .use { it.readText() }

        assertTrue(template.contains("val {{ parameter.name }}: {{ parameter.type }}"))
        assertTrue(template.contains("ConstraintValidator<{{ annotationName }}, {{ valueType }}>"))
        assertEquals("AuthorId", item.context["valueType"])
        assertEquals(
            listOf(mapOf("name" to "minimumLength", "type" to "Int", "defaultValue" to "3")),
            item.context["parameters"],
        )
    }

    private fun writeManifest(content: String): Path {
        val path = tempDir.resolve("validators.json")
        path.writeText(content)
        return path
    }

    private fun validManifest(valueType: String = "String"): String =
        """
        [
          {
            "package": "author",
            "name": "ValidAuthor",
            "message": "author is invalid",
            "targets": ["FIELD"],
            "valueType": "$valueType"
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
