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
        val manifestPath = Path.of(manifestFile)
        require(manifestPath.isAbsolute) {
            "only-engine-validator option manifestFile must be an absolute path"
        }

        val config = context.config
        val moduleRoot = config.modules["application"]
            ?: throw IllegalArgumentException(
                "project.applicationModulePath is required when only-engine validator addon is installed."
            )
        val artifactLayout = ArtifactLayoutResolver(config.basePackage, config.artifactLayout)
        val typeResolver = config.validatorTypeResolver(context.model)
        val templateId = "addons/only-engine-validator/validator.kt.peb"

        return ValidatorManifestParser.parse(manifestPath).map { entry ->
            val renderModel = entry.toRenderModel(config, typeResolver)
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

    private fun ProjectConfig.validatorTypeResolver(model: CanonicalModel): ValidatorTypeResolver {
        val candidates = mutableListOf<Pair<String, String>>()
        typeRegistryFqns().forEach { (name, fqn) -> candidates += name to fqn }
        model.typeRegistry.entries.forEach { (name, entry) -> candidates += name to entry.fqn }
        model.strongIds.forEach { strongId ->
            candidates += strongId.typeName to "${strongId.packageName}.${strongId.typeName}"
        }

        val fqnsBySimpleName = candidates
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.toSet() }
        return ValidatorTypeResolver(fqnsBySimpleName)
    }

    private fun ValidatorManifestEntry.toRenderModel(
        config: ProjectConfig,
        typeResolver: ValidatorTypeResolver,
    ): ValidatorRenderModel {
        val imports = linkedSetOf<String>()
        val packageName = if (packageName.isUnderBasePackage(config.basePackage)) {
            packageName
        } else {
            ArtifactLayoutResolver.joinPackage(config.basePackage, "application.validators", packageName)
        }
        val renderedValueType = valueType.renderType(typeResolver, imports)
        val renderedParameters = parameters.map { parameter ->
            mapOf(
                "name" to parameter.name,
                "type" to parameter.type,
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
            val packageName = entry.requiredString("package", index)
            validatePackageName(packageName, "validator manifest entry[$index].package")
            val name = entry.requiredString("name", index)
            validateKotlinIdentifier(name, "validator manifest entry[$index].name")
            ValidatorManifestEntry(
                packageName = packageName,
                name = name,
                description = entry.optionalString("desc", index),
                message = entry.requiredString("message", index),
                targets = entry.requiredStringList("targets", index).also { targets ->
                    require(targets.isNotEmpty()) {
                        "validator manifest entry[$index].targets must be non-empty"
                    }
                    targets.forEachIndexed { targetIndex, target ->
                        require(target in supportedAnnotationTargets) {
                            "validator manifest entry[$index].targets[$targetIndex] $target is not a supported AnnotationTarget"
                        }
                    }
                },
                valueType = entry.requiredString("valueType", index).also { valueType ->
                    validateBroadType(valueType, "validator manifest entry[$index].valueType")
                },
                parameters = entry.optionalObjectList("parameters", index).mapIndexed { parameterIndex, parameter ->
                    val parameterName = parameter.requiredString("name", index, parameterIndex)
                    validateKotlinIdentifier(parameterName, "validator manifest entry[$index].parameters[$parameterIndex].name")
                    val parameterType = parameter.requiredString("type", index, parameterIndex)
                    validateAnnotationParameterType(parameterType, index, parameterIndex)
                    val defaultValue = parameter.optionalString("defaultValue", index, parameterIndex)
                    validateAnnotationParameterDefault(defaultValue, parameterType, index, parameterIndex)
                    ValidatorManifestParameter(
                        name = parameterName,
                        type = parameterType,
                        defaultValue = defaultValue,
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

private val supportedAnnotationParameterTypes = setOf(
    "String",
    "Boolean",
    "Byte",
    "Short",
    "Int",
    "Long",
    "Float",
    "Double",
    "Char",
)

private val supportedAnnotationTargets = setOf(
    "FIELD",
    "VALUE_PARAMETER",
    "PROPERTY",
    "PROPERTY_GETTER",
    "PROPERTY_SETTER",
    "FUNCTION",
    "CLASS",
    "ANNOTATION_CLASS",
    "CONSTRUCTOR",
    "FILE",
    "TYPE",
    "TYPE_PARAMETER",
    "EXPRESSION",
    "LOCAL_VARIABLE",
)

private val kotlinHardKeywords = setOf(
    "as",
    "break",
    "class",
    "continue",
    "do",
    "else",
    "false",
    "for",
    "fun",
    "if",
    "in",
    "interface",
    "is",
    "null",
    "object",
    "package",
    "return",
    "super",
    "this",
    "throw",
    "true",
    "try",
    "typealias",
    "typeof",
    "val",
    "var",
    "when",
    "while",
)

private class ValidatorTypeResolver(
    private val fqnsBySimpleName: Map<String, Set<String>>,
) {
    fun resolve(simpleName: String): String? {
        val fqns = fqnsBySimpleName[simpleName] ?: return null
        require(fqns.size == 1) {
            "Ambiguous validator type reference: $simpleName"
        }
        return fqns.single()
    }
}

private fun String.renderType(typeResolver: ValidatorTypeResolver, imports: MutableSet<String>): String {
    val trimmed = trim()
    val nullable = trimmed.endsWith("?")
    val core = trimmed.removeSuffix("?")
    val genericStart = core.indexOf('<')
    val rendered = if (genericStart >= 0 && core.endsWith(">")) {
        val root = core.substring(0, genericStart).renderType(typeResolver, imports)
        val arguments = splitGenericArguments(core.substring(genericStart + 1, core.length - 1))
            .joinToString(", ") { it.renderType(typeResolver, imports) }
        "$root<$arguments>"
    } else {
        renderSimpleType(core, typeResolver, imports)
    }
    return if (nullable) "$rendered?" else rendered
}

private fun renderSimpleType(
    type: String,
    typeResolver: ValidatorTypeResolver,
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
    val fqn = typeResolver.resolve(trimmed)
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

private fun String.isUnderBasePackage(basePackage: String): Boolean =
    this == basePackage || startsWith("$basePackage.")

private fun validateAnnotationParameterType(
    type: String,
    entryIndex: Int,
    parameterIndex: Int,
) {
    require(type.trim() == type && type in supportedAnnotationParameterTypes) {
        "validator manifest entry[$entryIndex].parameters[$parameterIndex].type $type is not a supported annotation parameter type"
    }
}

private fun validateAnnotationParameterDefault(
    defaultValue: String?,
    type: String,
    entryIndex: Int,
    parameterIndex: Int,
) {
    if (defaultValue == null) {
        return
    }

    val isValid = when (type) {
        "Boolean" -> defaultValue == "true" || defaultValue == "false"
        "String" -> defaultValue.isKotlinStringLiteral()
        "Char" -> defaultValue.isKotlinCharLiteral()
        "Byte", "Short", "Int" -> intLiteralRegex.matches(defaultValue)
        "Long" -> longLiteralRegex.matches(defaultValue)
        "Float" -> floatLiteralRegex.matches(defaultValue)
        "Double" -> doubleLiteralRegex.matches(defaultValue)
        else -> false
    }
    require(isValid) {
        "validator manifest entry[$entryIndex].parameters[$parameterIndex].defaultValue $defaultValue is not a valid $type literal"
    }
}

private fun validateBroadType(type: String, label: String) {
    val trimmed = type.trim()
    require(trimmed == type && trimmed.isNotEmpty()) {
        "$label must be a non-blank Kotlin type"
    }
    validateBroadTypeCore(trimmed.removeSuffix("?"), label)
}

private fun validateBroadTypeCore(type: String, label: String) {
    val genericStart = type.indexOf('<')
    if (genericStart >= 0) {
        require(type.endsWith(">") && genericStart > 0) {
            "$label $type is not a valid Kotlin type"
        }
        validateSimpleOrFqnType(type.substring(0, genericStart), label)
        val arguments = splitGenericArguments(type.substring(genericStart + 1, type.length - 1))
        require(arguments.isNotEmpty()) {
            "$label $type is not a valid Kotlin type"
        }
        arguments.forEach { argument ->
            validateBroadType(argument, label)
        }
        return
    }
    validateSimpleOrFqnType(type, label)
}

private fun validateSimpleOrFqnType(type: String, label: String) {
    require(type.isNotBlank()) {
        "$label must be a non-blank Kotlin type"
    }
    if (type.contains('.')) {
        validatePackageName(type, label)
    } else {
        validateKotlinIdentifier(type, label)
    }
}

private fun validatePackageName(packageName: String, label: String) {
    val segments = packageName.split('.')
    require(segments.all { it.isNotBlank() }) {
        "$label must contain non-blank package segments"
    }
    segments.forEach { segment ->
        validateKotlinIdentifier(segment, "$label segment $segment")
    }
}

private fun validateKotlinIdentifier(value: String, label: String) {
    require(kotlinIdentifierRegex.matches(value) && value !in kotlinHardKeywords) {
        "$label is not a valid Kotlin identifier"
    }
}

private fun String.isKotlinStringLiteral(): Boolean {
    if (startsWith("\"\"\"") && endsWith("\"\"\"") && length >= 6) {
        return '$' !in substring(3, length - 3)
    }
    if (!startsWith('"') || !endsWith('"') || length < 2) {
        return false
    }
    return hasValidEscapedBody(1, length - 1, allowSinglePlainChar = false)
}

private fun String.isKotlinCharLiteral(): Boolean {
    if (!startsWith('\'') || !endsWith('\'') || length < 3) {
        return false
    }
    val bodyStart = 1
    val bodyEnd = length - 1
    if (this[bodyStart] != '\\') {
        return bodyEnd - bodyStart == 1 && this[bodyStart] !in setOf('\'', '\r', '\n')
    }
    return hasValidEscapedBody(bodyStart, bodyEnd, allowSinglePlainChar = true)
}

private fun String.hasValidEscapedBody(
    startIndex: Int,
    endIndex: Int,
    allowSinglePlainChar: Boolean,
): Boolean {
    var index = startIndex
    var plainChars = 0
    while (index < endIndex) {
        val char = this[index]
        if (char == '\r' || char == '\n') {
            return false
        }
        if (char != '\\') {
            if (char == '"' || char == '$') {
                return false
            }
            plainChars++
            index++
            continue
        }

        if (index + 1 >= endIndex) {
            return false
        }
        val escaped = this[index + 1]
        index += if (escaped == 'u') {
            if (index + 5 >= endIndex || !substring(index + 2, index + 6).all { it.isHexDigit() }) {
                return false
            }
            6
        } else {
            if (escaped !in setOf('b', 't', 'n', 'r', '\'', '"', '\\', '$')) {
                return false
            }
            2
        }
        plainChars++
    }
    return !allowSinglePlainChar || plainChars == 1
}

private fun Char.isHexDigit(): Boolean =
    this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'

private val kotlinIdentifierRegex = Regex("[A-Za-z_][A-Za-z0-9_]*")
private val intLiteralRegex = Regex("""[-+]?(?:0|[1-9][0-9_]*)""")
private val longLiteralRegex = Regex("""[-+]?(?:0|[1-9][0-9_]*)(?:[lL])?""")
private val exponentPart = """(?:[eE][-+]?(?:0|[1-9][0-9_]*))"""
private val floatBody =
    """(?:(?:0|[1-9][0-9_]*)\.(?:[0-9][0-9_]*)?|\.(?:[0-9][0-9_]*)|(?:0|[1-9][0-9_]*)$exponentPart)"""
private val floatLiteralRegex = Regex("""[-+]?(?:$floatBody|(?:0|[1-9][0-9_]*))[fF]""")
private val doubleLiteralRegex = Regex("""[-+]?$floatBody""")
