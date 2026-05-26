package com.only.engine.cap4k.addon.validation

import com.only4.cap4k.plugin.pipeline.api.ArtifactAddonContext
import com.only4.cap4k.plugin.pipeline.api.ArtifactAddonProvider
import com.only4.cap4k.plugin.pipeline.api.ArtifactLayoutResolver
import com.only4.cap4k.plugin.pipeline.api.ArtifactOutputKind
import com.only4.cap4k.plugin.pipeline.api.ArtifactPlanItem
import com.only4.cap4k.plugin.pipeline.api.CanonicalModel
import com.only4.cap4k.plugin.pipeline.api.ProjectConfig
import java.nio.file.Files
import java.nio.file.Path

class OnlyEngineValidatorAddonProvider : ArtifactAddonProvider {
    override val id: String = "only-engine-validator"

    override fun plan(context: ArtifactAddonContext): List<ArtifactPlanItem> {
        val manifestFile = context.options["manifestFile"]?.toString()?.trim().orEmpty()
        require(manifestFile.isNotBlank()) {
            "only-engine-validator requires option manifestFile"
        }

        val config = context.config
        val moduleRoot = config.modules["application"]
            ?: throw IllegalArgumentException(
                "project.applicationModulePath is required when only-engine validator addon is installed."
            )
        val artifactLayout = ArtifactLayoutResolver(config.basePackage, config.artifactLayout)
        val typeRegistry = config.validatorTypeRegistryFqns(context.model)
        val templateId = "addons/only-engine-validator/validator.kt.peb"

        return ValidatorManifestParser.parse(Path.of(manifestFile)).map { entry ->
            val renderModel = entry.toRenderModel(config, typeRegistry)
            ArtifactPlanItem(
                generatorId = id,
                moduleRole = "application",
                templateId = templateId,
                outputPath = artifactLayout.kotlinSourcePath(moduleRoot, renderModel.packageName, renderModel.annotationName),
                context = renderModel.toContext(),
                conflictPolicy = config.templates.templateConflictPolicies[templateId] ?: config.templates.conflictPolicy,
                outputKind = ArtifactOutputKind.CHECKED_IN_SOURCE,
                resolvedOutputRoot = artifactLayout.kotlinSourceRoot(moduleRoot),
            )
        }
    }

    private fun ProjectConfig.validatorTypeRegistryFqns(model: CanonicalModel): Map<String, String> =
        typeRegistryFqns() +
            model.typeRegistry.entries.mapValues { it.value.fqn } +
            model.strongIds.associate { strongId ->
                strongId.typeName to "${strongId.packageName}.${strongId.typeName}"
            }

    private fun ValidatorManifestEntry.toRenderModel(
        config: ProjectConfig,
        typeRegistry: Map<String, String>,
    ): ValidatorRenderModel {
        val imports = linkedSetOf<String>()
        val packageName = if (packageName.isFqn()) {
            packageName
        } else {
            ArtifactLayoutResolver.joinPackage(config.basePackage, "application.validators", packageName)
        }
        val renderedValueType = valueType.renderType(typeRegistry, imports)
        val renderedParameters = parameters.map { parameter ->
            mapOf(
                "name" to parameter.name,
                "type" to parameter.type.renderType(typeRegistry, imports),
                "defaultValue" to parameter.defaultValue,
            )
        }

        return ValidatorRenderModel(
            packageName = packageName,
            annotationName = name,
            description = description.orEmpty(),
            message = message,
            targets = targets,
            valueType = renderedValueType,
            parameters = renderedParameters,
            imports = imports.sorted(),
        )
    }
}

private data class ValidatorManifestEntry(
    val packageName: String,
    val name: String,
    val description: String?,
    val message: String,
    val targets: List<String>,
    val valueType: String,
    val parameters: List<ValidatorManifestParameter>,
)

private data class ValidatorManifestParameter(
    val name: String,
    val type: String,
    val defaultValue: String?,
)

private data class ValidatorRenderModel(
    val packageName: String,
    val annotationName: String,
    val description: String,
    val message: String,
    val targets: List<String>,
    val valueType: String,
    val parameters: List<Map<String, String?>>,
    val imports: List<String>,
) {
    fun toContext(): Map<String, Any?> =
        mapOf(
            "packageName" to packageName,
            "annotationName" to annotationName,
            "typeName" to annotationName,
            "description" to description,
            "message" to message,
            "targets" to targets,
            "valueType" to valueType,
            "parameters" to parameters,
            "imports" to imports,
        )
}

private object ValidatorManifestParser {
    fun parse(path: Path): List<ValidatorManifestEntry> {
        val root = SimpleJsonParser(Files.readString(path)).parse()
        val entries = root as? List<*> ?: throw IllegalArgumentException("validator manifest must be a JSON array")
        return entries.mapIndexed { index, value ->
            val entry = value.asObject("entry[$index]")
            ValidatorManifestEntry(
                packageName = entry.requiredString("package", index),
                name = entry.requiredString("name", index),
                description = entry.optionalString("desc", index),
                message = entry.requiredString("message", index),
                targets = entry.requiredStringList("targets", index).also { targets ->
                    require(targets.isNotEmpty()) {
                        "validator manifest entry[$index].targets must be non-empty"
                    }
                },
                valueType = entry.requiredString("valueType", index),
                parameters = entry.optionalObjectList("parameters", index).mapIndexed { parameterIndex, parameter ->
                    ValidatorManifestParameter(
                        name = parameter.requiredString("name", index, parameterIndex),
                        type = parameter.requiredString("type", index, parameterIndex),
                        defaultValue = parameter.optionalString("defaultValue", index, parameterIndex),
                    )
                },
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Any?.asObject(label: String): Map<String, Any?> =
        this as? Map<String, Any?> ?: throw IllegalArgumentException("validator manifest $label must be an object")

    private fun Map<String, Any?>.requiredString(
        field: String,
        entryIndex: Int,
        parameterIndex: Int? = null,
    ): String {
        val value = this[field] as? String
        require(!value.isNullOrBlank()) {
            fieldMessage(field, entryIndex, parameterIndex, "is required")
        }
        return value
    }

    private fun Map<String, Any?>.optionalString(
        field: String,
        entryIndex: Int,
        parameterIndex: Int? = null,
    ): String? {
        val value = this[field] ?: return null
        require(value is String) {
            fieldMessage(field, entryIndex, parameterIndex, "must be a string")
        }
        return value
    }

    private fun Map<String, Any?>.requiredStringList(field: String, entryIndex: Int): List<String> {
        val values = this[field] as? List<*> ?: throw IllegalArgumentException(
            fieldMessage(field, entryIndex, null, "must be an array")
        )
        return values.mapIndexed { valueIndex, value ->
            require(value is String && value.isNotBlank()) {
                "validator manifest entry[$entryIndex].$field[$valueIndex] must be a non-blank string"
            }
            value
        }
    }

    private fun Map<String, Any?>.optionalObjectList(field: String, entryIndex: Int): List<Map<String, Any?>> {
        val value = this[field] ?: return emptyList()
        val values = value as? List<*> ?: throw IllegalArgumentException(
            fieldMessage(field, entryIndex, null, "must be an array")
        )
        return values.mapIndexed { parameterIndex, parameter ->
            parameter.asObject("entry[$entryIndex].$field[$parameterIndex]")
        }
    }

    private fun fieldMessage(
        field: String,
        entryIndex: Int,
        parameterIndex: Int?,
        suffix: String,
    ): String {
        val prefix = if (parameterIndex == null) {
            "validator manifest entry[$entryIndex].$field"
        } else {
            "validator manifest entry[$entryIndex].parameters[$parameterIndex].$field"
        }
        return "$prefix $suffix"
    }
}

private class SimpleJsonParser(private val text: String) {
    private var index = 0

    fun parse(): Any? {
        val value = parseValue()
        skipWhitespace()
        require(index == text.length) {
            "unexpected JSON content at offset $index"
        }
        return value
    }

    private fun parseValue(): Any? {
        skipWhitespace()
        require(index < text.length) {
            "unexpected end of JSON"
        }
        return when (val char = text[index]) {
            '{' -> parseObject()
            '[' -> parseArray()
            '"' -> parseString()
            't' -> parseLiteral("true", true)
            'f' -> parseLiteral("false", false)
            'n' -> parseLiteral("null", null)
            '-', in '0'..'9' -> parseNumber()
            else -> throw IllegalArgumentException("unexpected JSON character '$char' at offset $index")
        }
    }

    private fun parseObject(): Map<String, Any?> {
        expect('{')
        val result = linkedMapOf<String, Any?>()
        skipWhitespace()
        if (consume('}')) {
            return result
        }
        while (true) {
            skipWhitespace()
            val key = parseString()
            skipWhitespace()
            expect(':')
            result[key] = parseValue()
            skipWhitespace()
            if (consume('}')) {
                return result
            }
            expect(',')
        }
    }

    private fun parseArray(): List<Any?> {
        expect('[')
        val result = mutableListOf<Any?>()
        skipWhitespace()
        if (consume(']')) {
            return result
        }
        while (true) {
            result += parseValue()
            skipWhitespace()
            if (consume(']')) {
                return result
            }
            expect(',')
        }
    }

    private fun parseString(): String {
        expect('"')
        val result = StringBuilder()
        while (index < text.length) {
            val char = text[index++]
            when (char) {
                '"' -> return result.toString()
                '\\' -> result.append(parseEscape())
                else -> result.append(char)
            }
        }
        throw IllegalArgumentException("unterminated JSON string")
    }

    private fun parseEscape(): Char {
        require(index < text.length) {
            "unterminated JSON escape"
        }
        return when (val char = text[index++]) {
            '"', '\\', '/' -> char
            'b' -> '\b'
            'f' -> '\u000C'
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            'u' -> parseUnicodeEscape()
            else -> throw IllegalArgumentException("unsupported JSON escape \\$char at offset ${index - 1}")
        }
    }

    private fun parseUnicodeEscape(): Char {
        require(index + 4 <= text.length) {
            "unterminated JSON unicode escape"
        }
        val hex = text.substring(index, index + 4)
        index += 4
        return hex.toInt(16).toChar()
    }

    private fun parseLiteral(literal: String, value: Any?): Any? {
        require(text.startsWith(literal, index)) {
            "expected JSON literal $literal at offset $index"
        }
        index += literal.length
        return value
    }

    private fun parseNumber(): Number {
        val start = index
        if (text[index] == '-') {
            index++
        }
        while (index < text.length && text[index].isDigit()) {
            index++
        }
        if (index < text.length && text[index] == '.') {
            index++
            while (index < text.length && text[index].isDigit()) {
                index++
            }
        }
        if (index < text.length && text[index].lowercaseChar() == 'e') {
            index++
            if (index < text.length && text[index] in setOf('+', '-')) {
                index++
            }
            while (index < text.length && text[index].isDigit()) {
                index++
            }
        }
        val raw = text.substring(start, index)
        return if (raw.contains('.') || raw.contains('e', ignoreCase = true)) {
            raw.toDouble()
        } else {
            raw.toLong()
        }
    }

    private fun skipWhitespace() {
        while (index < text.length && text[index].isWhitespace()) {
            index++
        }
    }

    private fun expect(expected: Char) {
        skipWhitespace()
        require(index < text.length && text[index] == expected) {
            "expected '$expected' at JSON offset $index"
        }
        index++
    }

    private fun consume(expected: Char): Boolean {
        skipWhitespace()
        if (index < text.length && text[index] == expected) {
            index++
            return true
        }
        return false
    }
}

private val kotlinBuiltInTypeNames = setOf(
    "Any",
    "Array",
    "Boolean",
    "Byte",
    "Char",
    "Collection",
    "Double",
    "Float",
    "Int",
    "Iterable",
    "List",
    "Long",
    "Map",
    "MutableCollection",
    "MutableIterable",
    "MutableList",
    "MutableMap",
    "MutableSet",
    "Nothing",
    "Number",
    "Pair",
    "Sequence",
    "Set",
    "Short",
    "String",
    "Triple",
    "Unit",
)

private fun String.renderType(typeRegistry: Map<String, String>, imports: MutableSet<String>): String {
    val trimmed = trim()
    val nullable = trimmed.endsWith("?")
    val core = trimmed.removeSuffix("?")
    val genericStart = core.indexOf('<')
    val rendered = if (genericStart >= 0 && core.endsWith(">")) {
        val root = core.substring(0, genericStart).renderType(typeRegistry, imports)
        val arguments = splitGenericArguments(core.substring(genericStart + 1, core.length - 1))
            .joinToString(", ") { it.renderType(typeRegistry, imports) }
        "$root<$arguments>"
    } else {
        renderSimpleType(core, typeRegistry, imports)
    }
    return if (nullable) "$rendered?" else rendered
}

private fun renderSimpleType(
    type: String,
    typeRegistry: Map<String, String>,
    imports: MutableSet<String>,
): String {
    val trimmed = type.trim()
    if (trimmed in kotlinBuiltInTypeNames) {
        return trimmed
    }
    if (trimmed.contains('.')) {
        imports += trimmed
        return trimmed.substringAfterLast('.')
    }
    val fqn = typeRegistry[trimmed]
    if (fqn != null) {
        imports += fqn
    }
    return trimmed
}

private fun splitGenericArguments(text: String): List<String> {
    if (text.isBlank()) {
        return emptyList()
    }

    val result = mutableListOf<String>()
    var depth = 0
    var start = 0
    text.forEachIndexed { index, char ->
        when (char) {
            '<' -> depth++
            '>' -> depth--
            ',' -> if (depth == 0) {
                result += text.substring(start, index).trim()
                start = index + 1
            }
        }
    }
    result += text.substring(start).trim()
    return result.filter { it.isNotEmpty() }
}

private fun String.isFqn(): Boolean =
    split('.').size > 2 && all { it == '.' || it == '_' || it.isLetterOrDigit() }
