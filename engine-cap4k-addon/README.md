# engine-cap4k-addon

This module contributes only-engine artifacts to cap4k through the cap4k
artifact addon SPI. The addon depends on
`com.only4:cap4k-plugin-pipeline-api` at compile time only; cap4k provides the
SPI classes when it loads the addon.

## Addons

`only-engine-enum-translation` generates adapter-module enum translation
components for cap4k canonical shared and aggregate-local enums.

`only-engine-validator` generates application-module Bean Validation annotation
types from a JSON manifest. Install the addon provider with `cap4kAddon(...)`;
the `addons.provider("only-engine-validator")` block only supplies provider
options:

```kotlin
plugins {
    id("com.only4.cap4k")
}

cap4k {
    cap4kAddon("com.only.engine:engine-cap4k-addon:<version>")

    addons.provider("only-engine-validator") {
        option("manifestFile", layout.projectDirectory.file("validation/validators.json").asFile.absolutePath)
    }
}
```

Validator manifest entries are JSON objects:

```json
[
  {
    "package": "author",
    "name": "ValidAuthor",
    "desc": "Valid author id",
    "message": "author is invalid",
    "targets": ["FIELD", "VALUE_PARAMETER"],
    "valueType": "AuthorId",
    "parameters": [
      { "name": "minimumLength", "type": "Int", "defaultValue": "3" }
    ]
  }
]
```

`manifestFile` must be an absolute filesystem path because addon execution does
not have a stable project root.

If `package` is equal to `basePackage` or starts with `<basePackage>.`, it is
used as-is. Otherwise generated validators go under
`<basePackage>.application.validators.<package>`.

`valueType` uses the broad validator type resolver. It may use Kotlin built-in
types, fully qualified class names, cap4k project type-registry simple names,
canonical model type-registry simple names, or generated strong-id type names.
It must be non-null because the generated `Validator.isValid` signature adds
nullability. If the same simple name maps to multiple different FQNs, planning
fails instead of silently choosing one.

Annotation parameter `type` is intentionally narrower because Kotlin annotation
properties only support a limited type set. The validator manifest currently
supports only `String`, `Boolean`, `Byte`, `Short`, `Int`, `Long`, `Float`,
`Double`, and `Char` for custom parameters. Value objects, strong IDs, FQNs,
generic types, nullable types, `List`/`Map`/`Array`, and `KClass` are rejected at
plan time. Parameter names must be unique and cannot use Bean Validation
reserved constructor property names: `message`, `groups`, or `payload`.
Parameter `defaultValue` is optional; when present it must be a valid scalar
literal for the declared type. Numeric underscores must appear only between
digits, and `Byte`, `Short`, `Int`, and `Long` defaults must fit the declared
type range.

Generated validators are skeletons. `Validator.isValid` defaults to `true` and
does not contain business validation logic; fill in the validation behavior by
hand after generation.

## Same-Cycle Verification

When the required cap4k SPI snapshot has not been published yet, verify against
a local cap4k checkout with a composite build:

```powershell
.\gradlew.bat --no-daemon "-PonlyEngine.cap4kCompositePath=C:/path/to/cap4k" :engine-cap4k-addon:test
```

`onlyEngine.useMavenLocalCap4k=true` is kept for local snapshot checks after
publishing cap4k to Maven local, but CI should prefer the composite build before
the cap4k snapshot is available from the normal Maven repositories.
