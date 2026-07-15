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

MiniInjector is published to **GitHub Packages**. Add the repository and dependencies to your `build.gradle`:

```groovy
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/playernguyen/MiniInjector")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation 'com.github.playernguyen:miniinjector-runtime:1.0.0'
    annotationProcessor 'com.github.playernguyen:miniinjector-processor:1.0.0'
}
```

> GitHub Packages requires authentication even for public packages. Create a [personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens) with `read:packages` scope and store it in `~/.gradle/gradle.properties` as `gpr.token=ghp_...`.

## Build

```bash
./gradlew build        # compile + test all modules
./gradlew clean build  # clean first
```

Requires Java 11+ and Gradle 9.3.0+.

## Documentation

See [`docs/INSTRUCTION.md`](docs/INSTRUCTION.md) for the full guide — annotations, API reference, examples, design notes, and limitations.
