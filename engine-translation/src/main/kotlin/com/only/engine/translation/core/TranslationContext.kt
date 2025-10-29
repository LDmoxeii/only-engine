package com.only.engine.translation.core

/**
 * Thread-local context to cache batch translation results within a serialization scope.
 */
object TranslationContext {
    private data class Key(val type: String, val other: String)

    private class Scope {
        val grouped: MutableMap<Key, MutableMap<Any, Any?>> = mutableMapOf()
    }

    private val tl: ThreadLocal<ArrayDeque<Scope>> = ThreadLocal.withInitial { ArrayDeque() }

    fun beginScope() {
        tl.get().addLast(Scope())
    }

    fun endScope() {
        val stack = tl.get()
        if (stack.isNotEmpty()) stack.removeLast()
        if (stack.isEmpty()) tl.remove()
    }

    fun put(type: String, other: String, key: Any, value: Any?) {
        val stack = tl.get()
        if (stack.isEmpty()) return
        val scope = stack.last()
        val map = scope.grouped.getOrPut(Key(type, other)) { mutableMapOf() }
        map[key] = value
    }

    fun get(type: String, other: String, key: Any): Any? {
        val stack = tl.get()
        if (stack.isEmpty()) return null
        val scope = stack.last()
        val map = scope.grouped[Key(type, other)] ?: return null
        return map[key]
    }

    fun putAll(type: String, other: String, values: Map<Any, Any?>) {
        val stack = tl.get()
        if (stack.isEmpty()) return
        val scope = stack.last()
        val map = scope.grouped.getOrPut(Key(type, other)) { mutableMapOf() }
        map.putAll(values)
    }
}

