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
        option("manifestFile", "validation/validators.json")
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

If `package` is fully qualified, it is used as-is. Otherwise generated validators
go under `<basePackage>.application.validators.<package>`. `valueType` and
parameter `type` values may use Kotlin built-ins, fully qualified class names,
registered type-registry simple names, or generated strong-id type names.

## Same-Cycle Verification

When the required cap4k SPI snapshot has not been published yet, verify against
a local cap4k checkout with a composite build:

```powershell
.\gradlew.bat --no-daemon "-PonlyEngine.cap4kCompositePath=C:/path/to/cap4k" :engine-cap4k-addon:test
```

`onlyEngine.useMavenLocalCap4k=true` is kept for local snapshot checks after
publishing cap4k to Maven local, but CI should prefer the composite build before
the cap4k snapshot is available from the normal Maven repositories.
