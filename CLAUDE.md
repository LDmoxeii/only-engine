# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

This is a Gradle-based Kotlin/Java multi-module project using the Gradle Wrapper:

- `./gradlew build` - Build all modules
- `./gradlew test` or `./gradlew check` - Run all tests and checks across modules
- `./gradlew clean` - Clean all build outputs
- `./gradlew :engine-common:test` - Run tests for a specific module
- `./gradlew :engine-web-starter:build` - Build a specific module
- `./gradlew publish` - Publish artifacts to configured repository (requires credentials)

## Architecture Overview

This is a Spring Boot framework library consisting of two main modules:

### engine-common
Core utilities and common classes:
- `Result<T>` - Standard API response wrapper with success/error handling
- Exception hierarchy (`ErrorException`, `KnownException`, `WarnException`)
- Enum-based status codes and constants
- Thread-local utilities for request context

### engine-web-starter
Spring Boot auto-configuration starter that provides:
- **Response Wrapping**: Automatically wraps controller responses in `Result<T>` format via `ResponseAdvice`
- **Global Exception Handling**: Converts exceptions to standardized error responses
- **I18n Support**: Internationalization with message resolution
- **Request Filtering**: Thread-local context management, health checks, request body wrapping
- **Authentication**: Basic auth check utilities

Key auto-configuration classes:
- `WebAutoConfiguration` - Main configuration entry point
- `ResponseAdvice` - Wraps responses unless annotated with `@IgnoreResultWrapper`
- `GlobalExceptionHandlerAdvice` - Handles exceptions globally

### Module Dependencies
- `engine-web-starter` depends on `engine-common`
- Uses Spring Boot 3.1.12, Kotlin 2.1.20, JDK 17
- Publishing configured for Aliyun Maven repository

### Convention Plugin
`buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts` contains shared build logic:
- Kotlin/JVM with Spring and JPA plugins
- Maven publishing setup
- Test configuration with JUnit 5
- JVM toolchain targeting Java 17

The auto-configuration enables by default but can be controlled via `only.engine.web.enable=false` property.
