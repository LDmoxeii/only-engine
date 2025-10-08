package com.only.engine.doc.config

import com.only.engine.doc.DocInitPrinter
import com.only.engine.doc.config.properties.SpringDocProperties
import com.only.engine.doc.handler.OpenApiHandler
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.apache.commons.lang3.StringUtils
import org.springdoc.core.configuration.SpringDocConfiguration
import org.springdoc.core.customizers.OpenApiBuilderCustomizer
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.customizers.ServerBaseUrlCustomizer
import org.springdoc.core.properties.SpringDocConfigProperties
import org.springdoc.core.providers.JavadocProvider
import org.springdoc.core.service.OpenAPIService
import org.springdoc.core.service.SecurityService
import org.springdoc.core.utils.PropertyResolverUtils
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import java.util.*

/**
 * 文档配置
 *
 * @author LD_moxeii
 */
@AutoConfiguration(before = [SpringDocConfiguration::class])
@EnableConfigurationProperties(SpringDocProperties::class)
@ConditionalOnProperty(prefix = "only.engine.doc", name = ["enable"], havingValue = "true")
class SpringDocConfiguration(
    private val serverProperties: ServerProperties,
) : DocInitPrinter {

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(SpringDocConfiguration::class.java)
    }

    /**
     * 配置 OpenAPI 文档基本信息
     */
    @Bean
    @ConditionalOnMissingBean(OpenAPI::class)
    fun openApi(properties: SpringDocProperties): OpenAPI {
        val openApi = OpenAPI()

        // 文档基本信息
        val infoProperties = properties.info
        val info = convertInfo(infoProperties)
        openApi.info(info)

        // 扩展文档信息
        openApi.externalDocs(properties.externalDocs)
        openApi.tags(properties.tags)
        openApi.paths(properties.paths)

        properties.components?.let { components ->
            openApi.components(components)

            // 配置安全认证
            components.securitySchemes.keys.let { keySet ->
                val securityRequirement = SecurityRequirement()
                keySet.forEach(securityRequirement::addList)
                openApi.security(listOf(securityRequirement))
            }
        }

        printInit(OpenAPI::class.java, log)
        return openApi
    }

    private fun convertInfo(infoProperties: SpringDocProperties.InfoProperties): Info {
        return Info().apply {
            title = infoProperties.title
            description = infoProperties.description
            contact = infoProperties.contact
            license = infoProperties.license
            version = infoProperties.version
        }
    }

    /**
     * 自定义 OpenAPI 处理器
     */
    @Bean
    fun openApiBuilder(
        openAPI: Optional<OpenAPI>,
        securityParser: SecurityService,
        springDocConfigProperties: SpringDocConfigProperties,
        propertyResolverUtils: PropertyResolverUtils,
        openApiBuilderCustomizers: Optional<List<OpenApiBuilderCustomizer>>,
        serverBaseUrlCustomizers: Optional<List<ServerBaseUrlCustomizer>>,
        javadocProvider: Optional<JavadocProvider>,
    ): OpenAPIService {
        return OpenApiHandler(
            openAPI,
            securityParser,
            springDocConfigProperties,
            propertyResolverUtils,
            openApiBuilderCustomizers,
            serverBaseUrlCustomizers,
            javadocProvider
        )
    }

    /**
     * 对已经生成好的 OpenAPI 进行自定义操作
     * 对所有路径增加前置上下文路径
     */
    @Bean
    fun openApiCustomizer(): OpenApiCustomizer {
        val contextPath = serverProperties.servlet.contextPath
        val finalContextPath = when {
            StringUtils.isBlank(contextPath) || "/" == contextPath -> ""
            else -> contextPath
        }

        return OpenApiCustomizer { openApi ->
            val oldPaths = openApi.paths
            if (oldPaths is PlusPaths) {
                return@OpenApiCustomizer
            }
            val newPaths = PlusPaths()
            oldPaths?.forEach { (k, v) -> newPaths.addPathItem(finalContextPath + k, v) }
            openApi.paths = newPaths
        }
    }

    /**
     * 单独使用一个类便于判断, 解决 springdoc 路径拼接重复问题
     *
     * @author LD_moxeii
     */
    class PlusPaths : Paths()
}
