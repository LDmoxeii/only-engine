package com.only.engine.redis

import com.only.engine.redis.misc.RedisUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

/**
 * Redis Codec 测试
 * 验证 JsonJacksonCodec 配置是否正确
 */
@SpringBootTest
class RedisCodecTest {

    data class SimpleObject(
        val name: String,
        val age: Int
    )

    data class ComplexObject(
        val id: Long,
        val title: String,
        val tags: List<String>,
        val metadata: Map<String, Any>
    )

    @Test
    fun `test simple object serialization should not add type info`() {
        // Given
        val obj = SimpleObject("测试对象", 25)
        val key = "codec:test:simple:${System.currentTimeMillis()}"

        // When
        RedisUtils.setCacheObject(key, obj)

        // Then: 应该能正确反序列化为 SimpleObject，而不是 LinkedHashMap
        val result = RedisUtils.getCacheObject<SimpleObject>(key)

        assertNotNull(result, "对象不应该为 null")
        assertEquals("测试对象", result!!.name, "name 字段应该正确")
        assertEquals(25, result.age, "age 字段应该正确")

        // 验证类型
        assertTrue(result is SimpleObject, "应该是 SimpleObject 类型，不应该是 LinkedHashMap")
        assertFalse(result is Map<*, *>, "不应该是 Map 类型")

        println("✅ 简单对象测试通过: $result")

        // Cleanup
        RedisUtils.deleteObject(key)
    }

    @Test
    fun `test complex object with collections`() {
        // Given
        val obj = ComplexObject(
            id = 1001L,
            title = "复杂对象测试",
            tags = listOf("Kotlin", "Redis", "Spring"),
            metadata = mapOf(
                "channel" to "TEST",
                "priority" to 10,
                "enabled" to true
            )
        )
        val key = "codec:test:complex:${System.currentTimeMillis()}"

        // When
        RedisUtils.setCacheObject(key, obj)

        // Then
        val result = RedisUtils.getCacheObject<ComplexObject>(key)

        assertNotNull(result)
        assertEquals(1001L, result!!.id)
        assertEquals("复杂对象测试", result.title)
        assertEquals(3, result.tags.size)
        assertTrue(result.tags.contains("Redis"))
        assertEquals("TEST", result.metadata["channel"])
        assertEquals(10, result.metadata["priority"])
        assertEquals(true, result.metadata["enabled"])

        // 验证类型
        assertTrue(result is ComplexObject, "应该是 ComplexObject 类型")

        println("✅ 复杂对象测试通过: $result")

        // Cleanup
        RedisUtils.deleteObject(key)
    }

    @Test
    fun `test nested object serialization`() {
        data class NestedObject(
            val name: String,
            val child: NestedObject? = null
        )

        // Given
        val nested = NestedObject("子对象")
        val obj = NestedObject("父对象", nested)
        val key = "codec:test:nested:${System.currentTimeMillis()}"

        // When
        RedisUtils.setCacheObject(key, obj)

        // Then
        val result = RedisUtils.getCacheObject<NestedObject>(key)

        assertNotNull(result)
        assertEquals("父对象", result!!.name)
        assertNotNull(result.child)
        assertEquals("子对象", result.child!!.name)

        println("✅ 嵌套对象测试通过: $result")

        // Cleanup
        RedisUtils.deleteObject(key)
    }

    @Test
    fun `test null values handling`() {
        data class NullableObject(
            val required: String,
            val optional: String? = null,
            val list: List<String>? = null
        )

        // Given
        val obj = NullableObject(required = "必填", optional = null, list = null)
        val key = "codec:test:nullable:${System.currentTimeMillis()}"

        // When
        RedisUtils.setCacheObject(key, obj)

        // Then
        val result = RedisUtils.getCacheObject<NullableObject>(key)

        assertNotNull(result)
        assertEquals("必填", result!!.required)
        assertNull(result.optional)
        assertNull(result.list)

        println("✅ Null 值处理测试通过: $result")

        // Cleanup
        RedisUtils.deleteObject(key)
    }

    @Test
    fun `test get and set with Redisson client directly`() {
        // Given
        val obj = SimpleObject("直接测试", 30)
        val key = "codec:test:direct:${System.currentTimeMillis()}"

        // When: 使用 RedissonClient 直接操作
        val client = RedisUtils.getClient()
        val bucket = client.getBucket<SimpleObject>(key)
        bucket.set(obj)

        // Then: 应该能正确获取
        val result = bucket.get()

        assertNotNull(result)
        assertEquals("直接测试", result.name)
        assertEquals(30, result.age)
        assertTrue(result is SimpleObject, "应该是 SimpleObject 类型，不是 LinkedHashMap")

        println("✅ 直接使用 Redisson 测试通过: $result")

        // Cleanup
        RedisUtils.deleteObject(key)
    }

    @Test
    fun `test multiple objects do not interfere with each other`() {
        // Given
        val obj1 = SimpleObject("对象1", 20)
        val obj2 = ComplexObject(
            id = 2001L,
            title = "对象2",
            tags = listOf("tag1", "tag2"),
            metadata = mapOf("key" to "value")
        )
        val key1 = "codec:test:multi1:${System.currentTimeMillis()}"
        val key2 = "codec:test:multi2:${System.currentTimeMillis()}"

        // When
        RedisUtils.setCacheObject(key1, obj1)
        RedisUtils.setCacheObject(key2, obj2)

        // Then
        val result1 = RedisUtils.getCacheObject<SimpleObject>(key1)
        val result2 = RedisUtils.getCacheObject<ComplexObject>(key2)

        assertNotNull(result1)
        assertNotNull(result2)
        assertEquals("对象1", result1!!.name)
        assertEquals(2001L, result2!!.id)

        println("✅ 多对象互不干扰测试通过")

        // Cleanup
        RedisUtils.deleteObject(key1)
        RedisUtils.deleteObject(key2)
    }
}
