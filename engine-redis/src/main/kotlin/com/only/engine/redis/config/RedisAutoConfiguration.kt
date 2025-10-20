package com.only.engine.redis.config

import cn.hutool.core.util.ObjectUtil
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.only.engine.redis.RedisInitPrinter
import com.only.engine.redis.config.properties.RedisProperties
import com.only.engine.redis.config.properties.RedissonProperties
import com.only.engine.redis.handler.KeyPrefixHandler
import org.redisson.client.codec.StringCodec
import org.redisson.codec.CompositeCodec
import org.redisson.codec.TypedJsonJacksonCodec
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Redis 自动配置
 *
 *
 * @author LD_moxeii
 */
@AutoConfiguration
@EnableConfigurationProperties(RedisProperties::class, RedissonProperties::class)
@ConditionalOnProperty(prefix = "only.engine.redis", name = ["enable"], havingValue = "true")
class RedisAutoConfiguration(
    private val redissonProperties: RedissonProperties,
) : RedisInitPrinter {

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(RedisAutoConfiguration::class.java)
    }

    init {
        printInit(RedisAutoConfiguration::class.java, log)
    }

    /**
     * 自定义 Redisson 配置
     */
    @Bean
    fun redissonCustomizer(): RedissonAutoConfigurationCustomizer {
        return RedissonAutoConfigurationCustomizer { config ->
            // 配置 JavaTimeModule 处理 LocalDateTime 序列化
            val javaTimeModule = JavaTimeModule()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            javaTimeModule.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(formatter))
            javaTimeModule.addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(formatter))

            // 配置 ObjectMapper
            val om = ObjectMapper().apply {
                // 注册 Kotlin 模块，支持 Kotlin data class 序列化/反序列化
                registerModule(KotlinModule.Builder().build())
                registerModule(javaTimeModule)
                setTimeZone(TimeZone.getDefault())
                setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
                // 指定序列化输入的类型，序列化时将对象全类名一起保存下来
                // 注意：Kotlin 的类默认是 final 的，使用 EVERYTHING 确保所有对象都添加类型信息
                // EVERYTHING: 为所有类型（包括 final 类）添加类型信息，适用于 Kotlin
                activateDefaultTyping(
                    LaissezFaireSubTypeValidator.instance,
                    ObjectMapper.DefaultTyping.EVERYTHING
                )
            }

            // 配置编解码器: key 使用 String, value 使用 TypedJsonJacksonCodec
            val jsonCodec = TypedJsonJacksonCodec(Any::class.java, om)
            val codec = CompositeCodec(StringCodec.INSTANCE, jsonCodec, jsonCodec)

            config.setThreads(redissonProperties.threads)
                .setNettyThreads(redissonProperties.nettyThreads)
                // 缓存 Lua 脚本,减少网络传输(Redisson 大部分功能都是基于 Lua 脚本实现)
                .setUseScriptCache(true).codec = codec

            // 单机模式配置
            val singleServerConfig = redissonProperties.singleServerConfig
            if (ObjectUtil.isNotNull(singleServerConfig)) {
                config.useSingleServer()
                    // 设置 Redis key 前缀
                    .setNameMapper(KeyPrefixHandler(redissonProperties.keyPrefix))
                    .setTimeout(singleServerConfig!!.timeout)
                    .setClientName(singleServerConfig.clientName)
                    .setIdleConnectionTimeout(singleServerConfig.idleConnectionTimeout)
                    .setSubscriptionConnectionPoolSize(singleServerConfig.subscriptionConnectionPoolSize)
                    .setConnectionMinimumIdleSize(singleServerConfig.connectionMinimumIdleSize).connectionPoolSize =
                    singleServerConfig.connectionPoolSize
            }

            // 集群模式配置
            val clusterServersConfig = redissonProperties.clusterServersConfig
            if (ObjectUtil.isNotNull(clusterServersConfig)) {
                config.useClusterServers()
                    // 设置 Redis key 前缀
                    .setNameMapper(KeyPrefixHandler(redissonProperties.keyPrefix))
                    .setTimeout(clusterServersConfig!!.timeout)
                    .setClientName(clusterServersConfig.clientName)
                    .setIdleConnectionTimeout(clusterServersConfig.idleConnectionTimeout)
                    .setSubscriptionConnectionPoolSize(clusterServersConfig.subscriptionConnectionPoolSize)
                    .setMasterConnectionMinimumIdleSize(clusterServersConfig.masterConnectionMinimumIdleSize)
                    .setMasterConnectionPoolSize(clusterServersConfig.masterConnectionPoolSize)
                    .setSlaveConnectionMinimumIdleSize(clusterServersConfig.slaveConnectionMinimumIdleSize)
                    .setSlaveConnectionPoolSize(clusterServersConfig.slaveConnectionPoolSize)
                    .setReadMode(clusterServersConfig.readMode).subscriptionMode = clusterServersConfig.subscriptionMode
            }

            printInit(RedissonAutoConfigurationCustomizer::class.java, log)
        }
    }

    /**
     * Redis 集群配置示例
     *
     * ```yaml
     * # Redis 集群配置(单机与集群只能开启一个,另一个需要注释掉)
     * spring:
     *   data:
     *     redis:
     *       cluster:
     *         nodes:
     *           - 192.168.0.100:6379
     *           - 192.168.0.101:6379
     *           - 192.168.0.102:6379
     *       password:
     *       timeout: 10s
     *       ssl:
     *         enabled: false
     *
     * only:
     *   engine:
     *     redisson:
     *       threads: 16
     *       nettyThreads: 32
     *       clusterServersConfig:
     *         clientName: ${app.name}
     *         masterConnectionMinimumIdleSize: 32
     *         masterConnectionPoolSize: 64
     *         slaveConnectionMinimumIdleSize: 32
     *         slaveConnectionPoolSize: 64
     *         idleConnectionTimeout: 10000
     *         timeout: 3000
     *         subscriptionConnectionPoolSize: 50
     *         readMode: "SLAVE"
     *         subscriptionMode: "MASTER"
     * ```
     */
}
