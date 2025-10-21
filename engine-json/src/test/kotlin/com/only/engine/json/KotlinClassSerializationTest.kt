package com.only.engine.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * KotlinModule 对各种 Kotlin 类的支持测试
 *
 * 测试场景：
 * 1. data class（主要使用场景）
 * 2. 普通 class with 主构造函数
 * 3. 普通 class with 次构造函数
 * 4. open class（可继承类）
 * 5. 嵌套类和内部类
 * 6. 可空类型和默认参数
 */
class KotlinClassSerializationTest {

    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
        }
    }

    // ==================== 测试用例类定义 ====================

    /**
     * 1. data class - 最常用的场景
     */
    data class UserVo(
        val id: Long,
        val name: String,
        val age: Int,
        val email: String?
    )

    /**
     * 2. 普通 class with 主构造函数（带 val/var）
     */
    class ApiResponse(
        val code: Int,
        val message: String,
        val data: Any?
    )

    /**
     * 3. 普通 class with 主构造函数（不带 val/var）- 无法序列化！
     */
    class InvalidClass(
        name: String,  // ← 没有 val/var，不是属性！
        age: Int
    )

    /**
     * 4. 普通 class with 次构造函数
     */
    class ProductVo {
        var id: Long = 0
        var name: String = ""
        var price: Double = 0.0

        constructor(id: Long, name: String, price: Double) {
            this.id = id
            this.name = name
            this.price = price
        }

        // 无参构造函数
        constructor()
    }

    /**
     * 5. open class（可被继承）
     */
    open class BaseEntity(
        val id: Long,
        val createdAt: Long
    )

    data class OrderVo(
        val orderId: Long,
        val userId: Long,
        val amount: Double
    ) : BaseEntity(orderId, System.currentTimeMillis())

    /**
     * 6. 带默认参数的类
     */
    data class ConfigVo(
        val name: String,
        val enabled: Boolean = true,
        val timeout: Int = 3000,
        val tags: List<String> = emptyList()
    )

    /**
     * 7. 嵌套 data class
     */
    data class PageResponse<T>(
        val code: Int,
        val message: String,
        val data: PageData<T>
    )

    data class PageData<T>(
        val list: List<T>,
        val total: Long,
        val pageNo: Int,
        val pageSize: Int
    )

    /**
     * 8. 包含 Map 的类
     */
    data class MetadataVo(
        val id: String,
        val properties: Map<String, Any>,
        val tags: List<String>
    )

    // ==================== 测试用例 ====================

    fun `test data class serialization and deserialization`() {
        // Given
        val user = UserVo(
            id = 1001L,
            name = "张三",
            age = 25,
            email = "zhangsan@example.com"
        )

        // When: 序列化
        val json = objectMapper.writeValueAsString(user)
        println("data class JSON: $json")

        // Then: 验证 JSON
        assertTrue(json.contains("\"id\":1001"))
        assertTrue(json.contains("\"name\":\"张三\""))
        assertTrue(json.contains("\"email\":\"zhangsan@example.com\""))

        // When: 反序列化
        val result = objectMapper.readValue<UserVo>(json)

        // Then: 验证对象
        assertEquals(1001L, result.id)
        assertEquals("张三", result.name)
        assertEquals(25, result.age)
        assertEquals("zhangsan@example.com", result.email)

        println("✅ data class 序列化/反序列化测试通过")
    }

    fun `test regular class with primary constructor`() {
        // Given
        val response = ApiResponse(
            code = 200,
            message = "Success",
            data = mapOf("userId" to 1001, "userName" to "张三")
        )

        // When: 序列化
        val json = objectMapper.writeValueAsString(response)
        println("Regular class JSON: $json")

        // Then: 验证 JSON
        assertTrue(json.contains("\"code\":200"))
        assertTrue(json.contains("\"message\":\"Success\""))

        // When: 反序列化
        val result = objectMapper.readValue<ApiResponse>(json)

        // Then: 验证对象
        assertEquals(200, result.code)
        assertEquals("Success", result.message)
        assertNotNull(result.data)

        println("✅ 普通 class with 主构造函数测试通过")
    }

    fun `test class with secondary constructor`() {
        // Given
        val product = ProductVo(
            id = 2001L,
            name = "笔记本电脑",
            price = 5999.99
        )

        // When: 序列化
        val json = objectMapper.writeValueAsString(product)
        println("Secondary constructor class JSON: $json")

        // Then: 验证 JSON
        assertTrue(json.contains("\"id\":2001"))
        assertTrue(json.contains("\"name\":\"笔记本电脑\""))
        assertTrue(json.contains("\"price\":5999.99"))

        // When: 反序列化（使用无参构造函数 + setter）
        val result = objectMapper.readValue<ProductVo>(json)

        // Then: 验证对象
        assertEquals(2001L, result.id)
        assertEquals("笔记本电脑", result.name)
        assertEquals(5999.99, result.price)

        println("✅ 次构造函数 class 测试通过")
    }

    fun `test class with nullable fields`() {
        // Given: email 为 null
        val json = """
            {
                "id": 1002,
                "name": "李四",
                "age": 30,
                "email": null
            }
        """.trimIndent()

        // When: 反序列化
        val result = objectMapper.readValue<UserVo>(json)

        // Then: 验证可空字段
        assertEquals(1002L, result.id)
        assertEquals("李四", result.name)
        assertEquals(30, result.age)
        assertNull(result.email, "email 应该为 null")

        println("✅ 可空字段测试通过")
    }

    fun `test class with default parameters`() {
        // Given: JSON 中缺少部分字段
        val json = """
            {
                "name": "my-config"
            }
        """.trimIndent()

        // When: 反序列化
        val result = objectMapper.readValue<ConfigVo>(json)

        // Then: 验证默认值
        assertEquals("my-config", result.name)
        assertEquals(true, result.enabled, "enabled 应该使用默认值 true")
        assertEquals(3000, result.timeout, "timeout 应该使用默认值 3000")
        assertEquals(emptyList<String>(), result.tags, "tags 应该使用默认值 emptyList()")

        println("✅ 默认参数测试通过")
    }

    fun `test nested data class`() {
        // Given
        val users = listOf(
            UserVo(1L, "张三", 25, "zhangsan@example.com"),
            UserVo(2L, "李四", 30, null)
        )
        val pageData = PageData(
            list = users,
            total = 100L,
            pageNo = 1,
            pageSize = 10
        )
        val response = PageResponse(
            code = 200,
            message = "Success",
            data = pageData
        )

        // When: 序列化
        val json = objectMapper.writeValueAsString(response)
        println("Nested data class JSON: $json")

        // Then: 验证 JSON
        assertTrue(json.contains("\"code\":200"))
        assertTrue(json.contains("\"total\":100"))
        assertTrue(json.contains("\"张三\""))

        // When: 反序列化（需要指定泛型类型）
        val result = objectMapper.readValue<PageResponse<UserVo>>(json)

        // Then: 验证嵌套对象
        assertEquals(200, result.code)
        assertEquals(100L, result.data.total)
        assertEquals(2, result.data.list.size)
        assertEquals("张三", result.data.list[0].name)

        println("✅ 嵌套 data class 测试通过")
    }

    fun `test class with map and list`() {
        // Given
        val metadata = MetadataVo(
            id = "meta-001",
            properties = mapOf(
                "channel" to "INLINE",
                "priority" to 10,
                "enabled" to true
            ),
            tags = listOf("kotlin", "jackson", "test")
        )

        // When: 序列化
        val json = objectMapper.writeValueAsString(metadata)
        println("Map & List class JSON: $json")

        // Then: 验证 JSON
        assertTrue(json.contains("\"channel\":\"INLINE\""))
        assertTrue(json.contains("\"kotlin\""))

        // When: 反序列化
        val result = objectMapper.readValue<MetadataVo>(json)

        // Then: 验证集合
        assertEquals("meta-001", result.id)
        assertEquals("INLINE", result.properties["channel"])
        assertEquals(10, result.properties["priority"])
        assertEquals(3, result.tags.size)
        assertTrue(result.tags.contains("jackson"))

        println("✅ Map 和 List 测试通过")
    }

    fun `test serialization of various kotlin types`() {
        // 测试各种类型的序列化兼容性
        val testCases = listOf(
            "data class" to UserVo(1L, "测试", 20, null),
            "regular class" to ApiResponse(200, "OK", null),
            "with secondary constructor" to ProductVo(1L, "商品", 99.99),
            "with defaults" to ConfigVo("config")
        )

        testCases.forEach { (description, obj) ->
            val json = objectMapper.writeValueAsString(obj)
            println("$description: $json")
            assertNotNull(json, "$description 序列化失败")
            assertTrue(json.isNotEmpty(), "$description 序列化结果为空")
        }

        println("✅ 多种 Kotlin 类型序列化测试通过")
    }

    fun `test invalid class without val or var should fail`() {
        // Given: 主构造函数参数没有 val/var
        val invalidObj = InvalidClass("测试", 20)

        // When: 序列化
        val json = objectMapper.writeValueAsString(invalidObj)
        println("Invalid class JSON: $json")

        // Then: 序列化会成功，但 JSON 是空对象（因为没有属性）
        assertEquals("{}", json, "没有 val/var 的构造函数参数不是属性，序列化为空对象")

        println("⚠️ 无属性类序列化为空对象")
    }

    fun `test api response scenario`() {
        // 模拟真实 API 响应场景
        data class ApiResult<T>(
            val code: Int,
            val message: String,
            val data: T?,
            val timestamp: Long = System.currentTimeMillis()
        )

        // Given: 成功响应
        val successResponse = ApiResult(
            code = 200,
            message = "操作成功",
            data = UserVo(1001L, "张三", 25, "zhangsan@example.com")
        )

        // When: 序列化
        val json = objectMapper.writeValueAsString(successResponse)
        println("API Success Response: $json")

        // Then: 验证
        assertTrue(json.contains("\"code\":200"))
        assertTrue(json.contains("\"message\":\"操作成功\""))
        assertTrue(json.contains("\"张三\""))

        // Given: 错误响应
        val errorResponse = ApiResult<Any>(
            code = 400,
            message = "参数错误",
            data = null
        )

        // When: 序列化
        val errorJson = objectMapper.writeValueAsString(errorResponse)
        println("API Error Response: $errorJson")

        // Then: 验证
        assertTrue(errorJson.contains("\"code\":400"))
        assertTrue(errorJson.contains("\"data\":null"))

        println("✅ API 响应场景测试通过")
    }
}
