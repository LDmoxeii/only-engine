package com.only.engine.factory

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.support.DefaultPropertySourceFactory
import org.springframework.core.io.support.EncodedResource
import java.io.IOException

/**
 * yml 配置源工厂
 *
 * @author LD_moxeii
 */
class YmlPropertySourceFactory : DefaultPropertySourceFactory() {

    @Throws(IOException::class)
    override fun createPropertySource(name: String?, resource: EncodedResource): PropertySource<*> {
        val sourceName = resource.resource.filename

        return if (sourceName != null && (sourceName.endsWith(".yml") || sourceName.endsWith(".yaml"))) {
            val factory = YamlPropertiesFactoryBean()
            factory.setResources(resource.resource)
            factory.afterPropertiesSet()
            PropertiesPropertySource(sourceName, factory.`object`!!)
        } else {
            super.createPropertySource(name, resource)
        }
    }
}
