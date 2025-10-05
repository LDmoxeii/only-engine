package com.only.engine.doc.handler

import cn.hutool.core.io.IoUtil
import io.swagger.v3.core.jackson.TypeNameResolver
import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.oas.annotations.tags.Tags
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.tags.Tag
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springdoc.core.customizers.OpenApiBuilderCustomizer
import org.springdoc.core.customizers.ServerBaseUrlCustomizer
import org.springdoc.core.properties.SpringDocConfigProperties
import org.springdoc.core.providers.JavadocProvider
import org.springdoc.core.service.OpenAPIService
import org.springdoc.core.service.SecurityService
import org.springdoc.core.utils.PropertyResolverUtils
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.util.CollectionUtils
import org.springframework.web.method.HandlerMethod
import java.io.StringReader
import java.lang.reflect.Method
import java.util.*

/**
 * 自定义 OpenAPI 处理器
 * 对源码功能进行修改增强使用
 *
 * @author LD_moxeii
 */
@Suppress("UNCHECKED_CAST")
class OpenApiHandler(
    openAPI: Optional<OpenAPI>,
    private val securityParser: SecurityService,
    springDocConfigProperties: SpringDocConfigProperties,
    private val propertyResolverUtils: PropertyResolverUtils,
    openApiBuilderCustomizers: Optional<List<OpenApiBuilderCustomizer>>,
    serverBaseUrlCustomizers: Optional<List<ServerBaseUrlCustomizer>>,
    private val javadocProvider: Optional<JavadocProvider>,
) : OpenAPIService(
    openAPI,
    securityParser,
    springDocConfigProperties,
    propertyResolverUtils,
    openApiBuilderCustomizers,
    serverBaseUrlCustomizers,
    javadocProvider
) {

    companion object {
        private val log = LoggerFactory.getLogger(OpenApiHandler::class.java)

        /**
         * The Basic error controller.
         */
        private var basicErrorController: Class<*>? = null
    }

    /**
     * The Mappings map.
     */
    private val mappingsMap: MutableMap<String, Any> = HashMap()

    /**
     * The Springdoc tags.
     */
    private val springdocTags: MutableMap<HandlerMethod, Tag> = HashMap()

    /**
     * The Cached open api map.
     */
    private val cachedOpenAPI: MutableMap<String, OpenAPI> = HashMap()

    /**
     * The Context.
     */
    private var context: ApplicationContext? = null

    /**
     * The Open api.
     */
    private var openAPI: OpenAPI? = null

    /**
     * The Is servers present.
     */
    private var isServersPresent: Boolean = false

    /**
     * The Server base url.
     */
    private var serverBaseUrl: String? = null

    init {
        if (openAPI.isPresent) {
            this.openAPI = openAPI.get().apply {
                if (components == null) {
                    components = Components()
                }
                if (paths == null) {
                    paths = Paths()
                }
                if (!CollectionUtils.isEmpty(servers)) {
                    isServersPresent = true
                }
            }
        }

        if (springDocConfigProperties.isUseFqn) {
            TypeNameResolver.std.useFqn = true
        }
    }

    override fun buildTags(
        handlerMethod: HandlerMethod,
        operation: Operation,
        openAPI: OpenAPI,
        locale: Locale,
    ): Operation {
        val tags = mutableSetOf<Tag>()
        var tagsStr = mutableSetOf<String>()

        buildTagsFromMethod(handlerMethod.method, tags, tagsStr, locale)
        buildTagsFromClass(handlerMethod.beanType, tags, tagsStr, locale)

        if (!CollectionUtils.isEmpty(tagsStr)) {
            tagsStr = tagsStr.asSequence()
                .map { propertyResolverUtils.resolve(it, locale) }
                .toMutableSet()
        }

        if (springdocTags.containsKey(handlerMethod)) {
            val tag = springdocTags[handlerMethod]!!
            tagsStr.add(tag.name)
            if (openAPI.tags == null || !openAPI.tags.contains(tag)) {
                openAPI.addTagsItem(tag)
            }
        }

        if (!CollectionUtils.isEmpty(tagsStr)) {
            if (CollectionUtils.isEmpty(operation.tags)) {
                operation.tags = ArrayList(tagsStr)
            } else {
                val operationTagsSet = HashSet(operation.tags)
                operationTagsSet.addAll(tagsStr)
                operation.tags.clear()
                operation.tags.addAll(operationTagsSet)
            }
        }

        if (isAutoTagClasses(operation)) {
            if (javadocProvider.isPresent) {
                val description = javadocProvider.get().getClassJavadoc(handlerMethod.beanType)
                if (StringUtils.isNotBlank(description)) {
                    val tag = Tag()

                    // 自定义部分: 修改使用 Java 注释当 tag 名
                    val list = IoUtil.readLines(StringReader(description), ArrayList())
                    tag.name = list[0]
                    operation.addTagsItem(list[0])

                    tag.description = description
                    if (openAPI.tags == null || !openAPI.tags.contains(tag)) {
                        openAPI.addTagsItem(tag)
                    }
                }
            } else {
                val tagAutoName = splitCamelCase(handlerMethod.beanType.simpleName)
                operation.addTagsItem(tagAutoName)
            }
        }

        if (!CollectionUtils.isEmpty(tags)) {
            // Existing tags
            val openApiTags = openAPI.tags
            if (!CollectionUtils.isEmpty(openApiTags)) {
                tags.addAll(openApiTags)
            }
            openAPI.tags = ArrayList(tags)
        }

        // Handle SecurityRequirement at operation level
        val securityRequirements = securityParser.getSecurityRequirements(handlerMethod)
        if (securityRequirements != null) {
            if (securityRequirements.isEmpty()) {
                operation.security = emptyList()
            } else {
                securityParser.buildSecurityRequirement(securityRequirements, operation)
            }
        }

        return operation
    }

    private fun buildTagsFromMethod(
        method: Method,
        tags: MutableSet<Tag>,
        tagsStr: MutableSet<String>,
        locale: Locale,
    ) {
        // method tags
        val tagsSet = AnnotatedElementUtils.findAllMergedAnnotations(method, Tags::class.java)
        val methodTags = tagsSet.asSequence()
            .flatMap { it.value.asSequence() }
            .toMutableSet()
        methodTags.addAll(
            AnnotatedElementUtils.findAllMergedAnnotations(
                method,
                io.swagger.v3.oas.annotations.tags.Tag::class.java
            )
        )

        if (!CollectionUtils.isEmpty(methodTags)) {
            tagsStr.addAll(
                methodTags.asSequence()
                    .map { propertyResolverUtils.resolve(it.name, locale) }
                    .filterNotNull()
                    .toSet()
            )
            val allTags = ArrayList(methodTags)
            addTags(allTags, tags, locale)
        }
    }

    private fun addTags(
        sourceTags: List<io.swagger.v3.oas.annotations.tags.Tag>,
        tags: MutableSet<Tag>,
        locale: Locale,
    ) {
        val optionalTagSet = AnnotationsUtils.getTags(
            sourceTags.toTypedArray(),
            true
        )
        optionalTagSet.ifPresent { tagsSet ->
            tagsSet.forEach { tag ->
                tag.name(propertyResolverUtils.resolve(tag.name, locale))
                tag.description(propertyResolverUtils.resolve(tag.description, locale))
                if (tags.none { it.name == tag.name }) {
                    tags.add(tag)
                }
            }
        }
    }
}
