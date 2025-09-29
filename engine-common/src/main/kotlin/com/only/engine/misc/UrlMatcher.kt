package com.only.engine.misc

import org.springframework.util.AntPathMatcher
import org.springframework.util.PathMatcher

object UrlMatcher {

    private val pathMatcher: PathMatcher = AntPathMatcher()

    fun matches(pattern: String, path: String): Boolean {
        return pathMatcher.match(pattern, path)
    }

    fun matchesAny(patterns: List<String>, path: String): Boolean {
        return patterns.any { pattern -> matches(pattern, path) }
    }

    fun isExcluded(excludePatterns: Array<String>, path: String): Boolean {
        return excludePatterns.any { pattern -> matches(pattern, path) }
    }

    fun filterMatching(patterns: List<String>, targetPattern: String): List<String> {
        return patterns.filter { pattern -> matches(targetPattern, pattern) }
    }
}
