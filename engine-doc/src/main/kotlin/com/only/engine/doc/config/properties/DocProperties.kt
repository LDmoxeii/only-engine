package com.only.engine.doc.config.properties

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Swagger 配置属性
 *
 * @author LD_moxeii
 */
@ConfigurationProperties(prefix = "only.engine.doc")
data class DocProperties(

    /**
     * 是否启用文档
     */
    var enable: Boolean = false,

    /**
     * 文档基本信息
     */
    var info: InfoProperties = InfoProperties(),

    /**
     * 扩展文档地址
     */
    var externalDocs: ExternalDocumentation? = null,

    /**
     * 标签
     */
    var tags: List<Tag>? = null,

    /**
     * 路径
     */
    var paths: Paths? = null,

    /**
     * 组件
     */
    var components: Components? = null,
) {

    /**
     * 文档的基础属性信息
     *
     * @see io.swagger.v3.oas.models.info.Info
     *
     * 为了 SpringBoot 自动生成配置提示信息,所以这里复制一个类出来
     */
    data class InfoProperties(
        /**
         * 标题
         */
        var title: String? = null,

        /**
         * 描述
         */
        var description: String? = null,

        /**
         * 联系人信息
         */
        var contact: Contact? = null,

        /**
         * 许可证
         */
        var license: License? = null,

        /**
         * 版本
         */
        var version: String? = null,
    )
}
