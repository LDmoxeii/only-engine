package com.only.engine.redis.config.properties

import org.redisson.config.ReadMode
import org.redisson.config.SubscriptionMode
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Redisson 配置属性
 *
 * @author LD_moxeii
 */
@ConfigurationProperties(prefix = "redisson")
data class RedissonProperties(
    /**
     * Redis 缓存 key 前缀
     */
    var keyPrefix: String? = null,

    /**
     * 线程池数量,默认值 = 当前处理核数量 * 2
     */
    var threads: Int = 0,

    /**
     * Netty 线程池数量,默认值 = 当前处理核数量 * 2
     */
    var nettyThreads: Int = 0,

    /**
     * 单机服务配置
     */
    var singleServerConfig: SingleServerConfig? = null,

    /**
     * 集群服务配置
     */
    var clusterServersConfig: ClusterServersConfig? = null,
) {

    /**
     * 单机服务配置
     */
    data class SingleServerConfig(
        /**
         * 客户端名称
         */
        var clientName: String? = null,

        /**
         * 最小空闲连接数
         */
        var connectionMinimumIdleSize: Int = 0,

        /**
         * 连接池大小
         */
        var connectionPoolSize: Int = 0,

        /**
         * 连接空闲超时,单位:毫秒
         */
        var idleConnectionTimeout: Int = 0,

        /**
         * 命令等待超时,单位:毫秒
         */
        var timeout: Int = 0,

        /**
         * 发布和订阅连接池大小
         */
        var subscriptionConnectionPoolSize: Int = 0,
    )

    /**
     * 集群服务配置
     */
    data class ClusterServersConfig(
        /**
         * 客户端名称
         */
        var clientName: String? = null,

        /**
         * Master 最小空闲连接数
         */
        var masterConnectionMinimumIdleSize: Int = 0,

        /**
         * Master 连接池大小
         */
        var masterConnectionPoolSize: Int = 0,

        /**
         * Slave 最小空闲连接数
         */
        var slaveConnectionMinimumIdleSize: Int = 0,

        /**
         * Slave 连接池大小
         */
        var slaveConnectionPoolSize: Int = 0,

        /**
         * 连接空闲超时,单位:毫秒
         */
        var idleConnectionTimeout: Int = 0,

        /**
         * 命令等待超时,单位:毫秒
         */
        var timeout: Int = 0,

        /**
         * 发布和订阅连接池大小
         */
        var subscriptionConnectionPoolSize: Int = 0,

        /**
         * 读取模式
         */
        var readMode: ReadMode? = null,

        /**
         * 订阅模式
         */
        var subscriptionMode: SubscriptionMode? = null,
    )
}
