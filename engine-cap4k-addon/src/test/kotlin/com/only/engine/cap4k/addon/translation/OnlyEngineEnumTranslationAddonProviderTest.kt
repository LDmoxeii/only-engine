package com.only.engine.cap4k.addon.translation

import com.only4.cap4k.plugin.pipeline.api.ArtifactAddonContext
import com.only4.cap4k.plugin.pipeline.api.ArtifactAddonProvider
import com.only4.cap4k.plugin.pipeline.api.ArtifactLayoutConfig
import com.only4.cap4k.plugin.pipeline.api.ArtifactOutputKind
import com.only4.cap4k.plugin.pipeline.api.CanonicalModel
import com.only4.cap4k.plugin.pipeline.api.ConflictPolicy
import com.only4.cap4k.plugin.pipeline.api.EntityModel
import com.only4.cap4k.plugin.pipeline.api.EnumItemModel
import com.only4.cap4k.plugin.pipeline.api.FieldModel
import com.only4.cap4k.plugin.pipeline.api.ProjectConfig
import com.only4.cap4k.plugin.pipeline.api.ProjectLayout
import com.only4.cap4k.plugin.pipeline.api.SharedEnumDefinition
import com.only4.cap4k.plugin.pipeline.api.TemplateConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.ServiceLoader

class OnlyEngineEnumTranslationAddonProviderTest {

    @Test
    fun `plans shared and local enum translation artifacts`() {
        val provider = OnlyEngineEnumTranslationAddonProvider()

        val items = provider.plan(context())

        assertEquals(listOf("only-engine-enum-translation", "only-engine-enum-translation"), items.map { it.generatorId })
        assertEquals(
            listOf(
                "demo-adapter/src/main/kotlin/com/acme/demo/adapter/domain/translation/shared/OrderStatusTranslation.kt",
                "demo-adapter/src/main/kotlin/com/acme/demo/adapter/domain/translation/order/OrderPriorityTranslation.kt",
            ),
            items.map { it.outputPath },
        )
        assertEquals(
            listOf(ArtifactOutputKind.CHECKED_IN_SOURCE, ArtifactOutputKind.CHECKED_IN_SOURCE),
            items.map { it.outputKind },
        )
        assertEquals(
            "addons/only-engine-enum-translation/aggregate/enum_translation.kt.peb",
            items.single { it.outputPath.endsWith("OrderStatusTranslation.kt") }.templateId,
        )
        assertEquals(
            ConflictPolicy.OVERWRITE,
            items.single { it.outputPath.endsWith("OrderStatusTranslation.kt") }.conflictPolicy,
        )
    }

    @Test
    fun `local enum translation scope follows entity table name not package tail`() {
        val provider = OnlyEngineEnumTranslationAddonProvider()

        val items = provider.plan(
            context(
                entityPackageName = "com.acme.custom.domain.model.post",
                entityTableName = "video_post",
            )
        )

        assertEquals(
            "demo-adapter/src/main/kotlin/com/acme/demo/adapter/domain/translation/video_post/OrderPriorityTranslation.kt",
            items.single { it.outputPath.endsWith("OrderPriorityTranslation.kt") }.outputPath,
        )
        assertEquals(
            "video_post_order_priority_code_to_desc",
            items.single { it.outputPath.endsWith("OrderPriorityTranslation.kt") }.context["translationTypeValue"],
        )
    }

    @Test
    fun `service loader metadata registers provider`() {
        val providers = ServiceLoader.load(ArtifactAddonProvider::class.java).toList()

        assertEquals(
            listOf("only-engine-enum-translation"),
            providers.map { it.id },
        )
    }

    @Test
    fun `addon template resource is packaged under cap4k addon namespace`() {
        assertNotNull(
            javaClass.classLoader.getResource(
                "cap4k/addons/only-engine-enum-translation/aggregate/enum_translation.kt.peb"
            )
        )
    }

    @Test
    fun `addon template rejects lossy enum code conversion`() {
        val template = javaClass.classLoader
            .getResource("cap4k/addons/only-engine-enum-translation/aggregate/enum_translation.kt.peb")!!
            .readText()

        assertFalse(template.contains("is Number -> key.toInt()"))
        assertTrue(template.contains("is Long -> takeIf { it in Int.MIN_VALUE..Int.MAX_VALUE }?.toInt()"))
        assertTrue(template.contains("is String -> trim().toIntOrNull()"))
        assertTrue(template.contains("else -> null"))
    }

    private fun context(
        entityPackageName: String = "order",
        entityTableName: String = "order",
    ): ArtifactAddonContext =
        ArtifactAddonContext(
            config = ProjectConfig(
                basePackage = "com.acme.demo",
                layout = ProjectLayout.MULTI_MODULE,
                modules = mapOf("adapter" to "demo-adapter"),
                sources = emptyMap(),
                generators = emptyMap(),
                templates = TemplateConfig("ddd-default", emptyList(), ConflictPolicy.OVERWRITE),
                artifactLayout = ArtifactLayoutConfig(),
            ),
            model = CanonicalModel(
                sharedEnums = listOf(
                    SharedEnumDefinition(
                        typeName = "OrderStatus",
                        packageName = "shared",
                        items = listOf(EnumItemModel(1, "OPEN", "open")),
                    )
                ),
                entities = listOf(
                    EntityModel(
                        name = "Order",
                        packageName = entityPackageName,
                        tableName = entityTableName,
                        comment = "",
                        fields = listOf(
                            FieldModel(
                                name = "id",
                                type = "Long",
                                nullable = false,
                            ),
                            FieldModel(
                                name = "priority",
                                type = "Int",
                                typeBinding = "OrderPriority",
                                enumItems = listOf(EnumItemModel(1, "HIGH", "high")),
                            )
                        ),
                        idField = FieldModel(
                            name = "id",
                            type = "Long",
                            nullable = false,
                        ),
                    )
                ),
            ),
        )
}
