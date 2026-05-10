package com.only.engine.cap4k.addon.translation

import com.only4.cap4k.plugin.pipeline.api.ArtifactAddonContext
import com.only4.cap4k.plugin.pipeline.api.ArtifactAddonProvider
import com.only4.cap4k.plugin.pipeline.api.ArtifactLayoutResolver
import com.only4.cap4k.plugin.pipeline.api.ArtifactOutputKind
import com.only4.cap4k.plugin.pipeline.api.ArtifactPlanItem
import com.only4.cap4k.plugin.pipeline.api.CanonicalEnumCatalog
import com.only4.cap4k.plugin.pipeline.api.CanonicalEnumDescriptor
import com.only4.cap4k.plugin.pipeline.api.ConflictPolicy
import com.only4.cap4k.plugin.pipeline.api.ProjectConfig
import java.util.Locale

class OnlyEngineEnumTranslationAddonProvider : ArtifactAddonProvider {
    override val id: String = "only-engine-enum-translation"

    override fun plan(context: ArtifactAddonContext): List<ArtifactPlanItem> {
        val artifactLayout = ArtifactLayoutResolver(context.config.basePackage, context.config.artifactLayout)
        val enumCatalog = CanonicalEnumCatalog.from(
            model = context.model,
            artifactLayout = artifactLayout,
            typeRegistry = context.config.typeRegistry,
        )
        val localOwnerScopes = context.model.entities
            .flatMap { entity ->
                entity.fields.mapNotNull { field ->
                    val typeBinding = field.typeBinding?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    if (field.enumItems.isEmpty()) return@mapNotNull null
                    LocalEnumKey(entity.packageName, typeBinding) to entity.tableName.lowercase(Locale.ROOT)
                }
            }
            .toMap()

        return enumCatalog.allEnums.map { descriptor ->
            descriptor.toPlanItem(context.config, artifactLayout, localOwnerScopes)
        }
    }

    private fun CanonicalEnumDescriptor.toPlanItem(
        config: ProjectConfig,
        artifactLayout: ArtifactLayoutResolver,
        localOwnerScopes: Map<LocalEnumKey, String>,
    ): ArtifactPlanItem {
        val moduleRoot = config.modules["adapter"]
            ?: throw IllegalArgumentException(
                "project.adapterModulePath is required when only-engine enum translation addon is installed."
            )
        val ownerScope = if (shared) {
            "shared"
        } else {
            localOwnerScopes[LocalEnumKey(ownerPackageName.orEmpty(), typeName)]
                ?: throw IllegalArgumentException("local enum translation owner scope is missing for $fqn")
        }
        val packageName = ArtifactLayoutResolver.joinPackage(
            config.basePackage,
            "adapter.domain.translation",
            ownerScope,
        )
        val translationTypeName = "${typeName}Translation"
        val typeKey = translationTypeKey(if (shared) "" else ownerScope, typeName)
        val translationTypeConst = "${typeKey.uppercase(Locale.ROOT)}_CODE_TO_DESC"
        val translationTypeValue = "${typeKey}_code_to_desc"
        val templateId = "addons/only-engine-enum-translation/aggregate/enum_translation.kt.peb"

        return ArtifactPlanItem(
            generatorId = id,
            moduleRole = "adapter",
            templateId = templateId,
            outputPath = artifactLayout.kotlinSourcePath(moduleRoot, packageName, translationTypeName),
            context = mapOf(
                "packageName" to packageName,
                "typeName" to translationTypeName,
                "enumTypeName" to typeName,
                "enumTypeFqn" to fqn,
                "translationTypeConst" to translationTypeConst,
                "translationTypeValue" to translationTypeValue,
            ),
            conflictPolicy = config.templates.templateConflictPolicies[templateId] ?: config.templates.conflictPolicy,
            outputKind = ArtifactOutputKind.CHECKED_IN_SOURCE,
            resolvedOutputRoot = artifactLayout.kotlinSourceRoot(moduleRoot),
        )
    }
}

private data class LocalEnumKey(
    val ownerPackageName: String,
    val typeBinding: String,
)

private fun translationTypeKey(ownerScope: String, typeName: String): String {
    val scopedTypeName = if (ownerScope.isBlank()) typeName else "${ownerScope}_$typeName"
    return scopedTypeName
        .replace(Regex("([a-z0-9])([A-Z])"), "$1_$2")
        .replace(Regex("([A-Z]+)([A-Z][a-z])"), "$1_$2")
        .replace("-", "_")
        .replace(".", "_")
        .lowercase(Locale.ROOT)
}
