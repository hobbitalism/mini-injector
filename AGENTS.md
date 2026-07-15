# AGENTS.md — MiniInjector

## Project

Multi-module Gradle (Java 11+, Gradle 9.3.0) compile-time DI framework. No reflection at runtime — annotation processor generates `*Provider` classes.

## Modules

| Module | Package | Purpose |
|---|---|---|
| `core/` | `com.github.playernguyen.inject` | Annotations (`@Component`, `@Inject`, `@Singleton`), `InjectionPoint` interface, `InjectionException` |
| `processor/` | `com.github.playernguyen.processor` | APT (`InjectionProcessor`) — generates `{Name}Provider` in `{pkg}.generated` sub-package |
| `runtime/` | `com.github.playernguyen.runtime` | `InjectionContainer`, `ContainerBuilder`, `ComponentScanner` (uses ClassGraph) |
| `examples:demoapp` | `com.github.playernguyen.demoapp` | Plain Java demo, main class `DemoApp` |
| `examples:minecraft` | `com.github.playernguyen.minecraft` | Spigot plugin demo (compile-only dep on spigot-api 1.20.1) |

## Commands

```bash
./gradlew build                # compile + test all modules
./gradlew clean build          # clean build
./gradlew :examples:demoapp:run  # run the demo app
```

No tests exist yet (`src/test/` is absent across all modules).

## Key conventions

- **Versioning:** SemVer (planned, see `.opencode/plans/deployment-plan.md`). Current version `1.0-SNAPSHOT`.
- **Commit style:** [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `chore:`, etc.).
- **Group ID:** `com.github.playernguyen`

## Architecture notes

- `@Component` can take an optional qualifier string (`@Component("name")`). Without it, the FQN is the key.
- `@Inject` on a constructor enables constructor injection; on a public field enables field injection. Only one `@Inject` constructor per class.
- `@Singleton` caches the instance. Without it, every `get()` creates a new instance (prototype scope).
- Generated providers go to `{pkg}.generated.{Name}Provider`. The constant `GENERATED_SUBPACKAGE = "generated"` is duplicated in both `InjectionProcessor` and `ComponentScanner` — **must keep in sync**.
- `ComponentScanner` also registers each component under every interface it directly implements (for interface-based injection).
- `ContainerBuilder` supports: `scanPackage(s)`, `withComponent()`, `withProvider()`, `withSingleton()`.
- Container is **not thread-safe** (plain `HashMap` for singleton cache).
- Field injection requires **public** fields (generated code sets fields directly).
- No circular dependency detection (will produce `StackOverflowError`).
- `InjectionPoint` interface: `Object provide(Object container)` — the `container` parameter is cast to `InjectionContainer` inside generated code.

## Gotchas

- `processor` must be declared as `annotationProcessor` (not `implementation`) in consumer projects, otherwise `container.get()` fails with "Generated provider not found".
- `auto-service` in `processor/build.gradle` needs both `implementation` and `annotationProcessor` declarations for self-registration via `META-INF/services`.
- No CI workflows exist yet (planned in deployment plan).
- No `gradle.properties` file exists yet (planned in deployment plan).
