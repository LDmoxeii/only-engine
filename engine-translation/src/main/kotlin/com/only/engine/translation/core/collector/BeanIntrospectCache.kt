package com.only.engine.translation.core.collector

import com.only.engine.translation.annotation.Translation
import java.beans.Introspector
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

data class TranslationFieldMeta(
    val propertyName: String,
    val type: String,
    val other: String,
    val mapper: String,
)

/**
 * Caches bean metadata and property accessors for @Translation fields to reduce reflection overhead.
 */
object BeanIntrospectCache {
    private val fieldMetaCache = ConcurrentHashMap<Class<*>, List<TranslationFieldMeta>>()
    private val pdCache = ConcurrentHashMap<Class<*>, Map<String, Method>>()
    private val fieldCache = ConcurrentHashMap<Class<*>, Map<String, Field>>()

    fun metasOf(clazz: Class<*>): List<TranslationFieldMeta> =
        fieldMetaCache.computeIfAbsent(clazz) { analyzeClass(it) }

    fun readProperty(bean: Any, name: String): Any? {
        val cls = bean.javaClass
        val getter = pdCache.computeIfAbsent(cls) {
            val map = mutableMapOf<String, Method>()
            try {
                val info = Introspector.getBeanInfo(cls)
                info.propertyDescriptors.forEach { pd ->
                    val m = pd.readMethod
                    if (m != null) map[pd.name] = m
                }
            } catch (_: Exception) {
            }
            map
        }[name]
        if (getter != null) {
            try {
                getter.isAccessible = true
                return getter.invoke(bean)
            } catch (_: Exception) {
            }
        }

        val fld = fieldCache.computeIfAbsent(cls) {
            val map = mutableMapOf<String, Field>()
            var c: Class<*>? = cls
            while (c != null && c != Any::class.java) {
                c.declaredFields.forEach { f -> map.putIfAbsent(f.name, f) }
                c = c.superclass
            }
            map
        }[name]
        if (fld != null) {
            try {
                fld.isAccessible = true
                return fld.get(bean)
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun analyzeClass(clazz: Class<*>): List<TranslationFieldMeta> {
        val metas = mutableMapOf<String, TranslationFieldMeta>()

        // Prefer getter annotation (Jackson property-based), fallback to field annotation
        runCatching {
            var c: Class<*>? = clazz
            while (c != null && c != Any::class.java) {
                c.declaredMethods.forEach { m ->
                    if (m.parameterCount == 0 && m.name.startsWith("get")) {
                        val ann = m.getAnnotation(Translation::class.java)
                        if (ann != null) {
                            val prop = getterToProperty(m.name)
                            metas[prop] = TranslationFieldMeta(prop, ann.type, ann.other, ann.mapper)
                        }
                    }
                }
                c = c.superclass
            }
        }

        runCatching {
            var c: Class<*>? = clazz
            while (c != null && c != Any::class.java) {
                c.declaredFields.forEach { f ->
                    val ann = f.getAnnotation(Translation::class.java)
                    if (ann != null) {
                        metas.putIfAbsent(f.name, TranslationFieldMeta(f.name, ann.type, ann.other, ann.mapper))
                    }
                }
                c = c.superclass
            }
        }

        return metas.values.toList()
    }

    private fun getterToProperty(getter: String): String {
        val name = getter.removePrefix("get")
        return name.replaceFirstChar { it.lowercaseChar() }
    }
}

