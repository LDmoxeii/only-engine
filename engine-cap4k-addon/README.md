# engine-cap4k-addon

This module contributes only-engine enum translation artifacts to cap4k through
the cap4k artifact addon SPI. The addon depends on
`com.only4:cap4k-plugin-pipeline-api` at compile time only; cap4k provides the
SPI classes when it loads the addon.

## Same-Cycle Verification

When the required cap4k SPI snapshot has not been published yet, verify against
a local cap4k checkout with a composite build:

```powershell
.\gradlew.bat --no-daemon "-PonlyEngine.cap4kCompositePath=C:/path/to/cap4k" :engine-cap4k-addon:test
```

`onlyEngine.useMavenLocalCap4k=true` is kept for local snapshot checks after
publishing cap4k to Maven local, but CI should prefer the composite build before
the cap4k snapshot is available from the normal Maven repositories.
