# MiniInjector

A lightweight dependency injection framework for Java. Providers are generated at compile time via an annotation processor — no runtime reflection.

## Modules

| Module | Purpose |
|---|---|
| `core` | Annotations (`@Component`, `@Inject`, `@Singleton`) and interfaces |
| `processor` | Annotation processor — generates `*Provider` classes at compile time |
| `runtime` | DI container, `ContainerBuilder`, `ComponentScanner` |
| `examples/demoapp` | Plain Java demo |
| `examples/minecraft` | Spigot plugin demo |

## Quick start

**1. Annotate your classes**

```java
@Component
@Singleton
public class UserRepository { ... }

@Component
public class UserService {
    @Inject
    public UserRepository repository;
}
```

**2. Build the container**

```java
InjectionContainer container = ContainerBuilder.create()
    .scanPackage("com.example")
    .build();

UserService service = (UserService) container.get(UserService.class.getName());
```

## Add as a dependency

MiniInjector is published via **JitPack**. Add the repository and dependencies to your `build.gradle`:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.playernguyen.MiniInjector:miniinjector-runtime:1.0.0'
    annotationProcessor 'com.github.playernguyen.MiniInjector:miniinjector-processor:1.0.0'
}
```

> The group ID is `com.github.playernguyen.MiniInjector` — JitPack derives it from the GitHub repository coordinates.

## Build

```bash
./gradlew build        # compile + test all modules
./gradlew clean build  # clean first
```

Requires Java 11+ and Gradle 9.3.0+.

## Documentation

See [`docs/INSTRUCTION.md`](docs/INSTRUCTION.md) for the full guide — annotations, API reference, examples, design notes, and limitations.
