# MiniInjector — Developer Guide

## Table of contents

1. [How it works](#how-it-works)
2. [Project structure](#project-structure)
3. [Annotations](#annotations)
4. [Building the container](#building-the-container)
5. [InjectionContainer API](#injectioncontainer-api)
6. [Examples](#examples)
7. [Error handling](#error-handling)
8. [Limitations](#limitations)
9. [Design notes](#design-notes)

---

## How it works

MiniInjector works in two phases.

**Compile time** — the annotation processor (`InjectionProcessor`) scans every class annotated with `@Component` and generates a corresponding `*Provider` class in a `generated` sub-package. The provider implements `InjectionPoint` and contains plain `new` calls — no reflection.

```
Your source code
  └─ @Component UserService
       │
       ▼  (javac + InjectionProcessor)
  com.example.generated.UserServiceProvider   ← generated, compiled with your code
```

**Runtime** — `ContainerBuilder` scans the target package with ClassGraph, loads each generated provider via `Class.forName`, and registers it in `InjectionContainer`. When you call `container.get(key)` the provider instantiates the object, injects constructor and field dependencies (by resolving them from the container), and returns it. `@Singleton` instances are cached after the first call.

```
container.get("com.example.UserService")
  └─ UserServiceProvider.provide(container)
       ├─ new UserService()
       ├─ instance.repository = container.get("com.example.UserRepository")
       └─ return instance
```

---

## Project structure

```
MiniInjector/
├── core/           Annotations (@Component, @Inject, @Singleton)
│                   and the InjectionPoint interface
├── processor/      Annotation processor — reads @Component, writes *Provider sources
├── runtime/        InjectionContainer, ContainerBuilder, ComponentScanner
└── examples/
    ├── demoapp/    Plain Java demo (UserController → UserService → UserRepository)
    └── minecraft/  Spigot plugin demo (MiniInjectorPlugin as entry point)
```

Generated providers land in `<your-package>.generated.*` — kept separate from user code to avoid name collisions with any hand-written class whose name happens to end in `Provider`.

---

## Annotations

### `@Component`

Marks a class as a DI-managed component. The processor generates a provider for it.

```java
@Component
public class UserService { }          // registered under its fully-qualified name

@Component("userSvc")
public class UserService { }          // registered under "userSvc"
```

The component is also registered under every interface it directly implements, so field/constructor injection by interface type works out of the box.

### `@Inject`

Marks a constructor or a field as an injection point.

```java
// Constructor injection — dependencies resolved from the container
@Inject
public UserService(UserRepository repo) {
    this.repo = repo;
}

// Field injection — field must be public
@Inject
public UserRepository repository;

// Named/qualifier injection
@Inject("primary")
public UserRepository repository;
```

Only one constructor should be marked `@Inject`. If none is marked, the no-arg constructor is used.

### `@Singleton`

Caches the instance after the first creation. All subsequent `container.get()` calls return the same object.

```java
@Component
@Singleton
public class DatabaseConnection { }
```

Without `@Singleton`, every `container.get()` call creates a new instance (prototype scope).

---

## Building the container

### Auto-scan (recommended)

```java
InjectionContainer container = ContainerBuilder.create()
    .scanPackage("com.example")          // discovers all @Component classes recursively
    .build();
```

### Manual registration

```java
InjectionContainer container = ContainerBuilder.create()
    .withComponent(UserRepository.class) // class must have @Component
    .withComponent(UserService.class)
    .build();
```

### Pre-built singletons

Useful for objects you construct yourself (e.g. a plugin instance, a config object):

```java
InjectionContainer container = ContainerBuilder.create()
    .withSingleton(MyPlugin.class.getName(), pluginInstance)
    .scanPackage("com.example")
    .build();
```

### Custom providers

For complex or conditional instantiation:

```java
InjectionContainer container = ContainerBuilder.create()
    .withProvider(DataSource.class, ctx -> {
        // custom creation logic
        return new HikariDataSource(config);
    })
    .scanPackage("com.example")
    .build();
```

### Multiple packages

```java
ContainerBuilder.create()
    .scanPackages("com.example.services", "com.example.repositories")
    .build();
```

---

## InjectionContainer API

```java
// Retrieve a singleton (cached after first call)
UserService svc = (UserService) container.get(UserService.class.getName());

// Retrieve a new instance every time (prototype)
UserService svc = (UserService) container.getPrototype(UserService.class.getName());

// Check registration
boolean registered = container.isRegistered("com.example.UserService");

// Register a provider manually
container.register("myKey", injectionPoint);

// Clear only the singleton cache (providers remain)
container.clearSingletons();

// Clear everything
container.clear();
```

---

## Examples

### Plain Java — `examples/demoapp`

```java
InjectionContainer container = ContainerBuilder.create()
    .scanPackage("com.github.playernguyen.demoapp")
    .build();

UserController controller =
    (UserController) container.get(UserController.class.getName());

controller.handleGetUser(1);
```

Run with:

```bash
./gradlew :examples:demoapp:run
```

### Spigot plugin — `examples/minecraft`

`MiniInjectorPlugin.onEnable()` builds the container, then pulls commands and listeners from it to wire into Bukkit:

```java
container = ContainerBuilder.create()
    .withSingleton(MiniInjectorPlugin.class.getName(), this)
    .scanPackage("com.github.playernguyen.minecraft")
    .build();

getCommand("playerinfo").setExecutor(
    (PlayerInfoCommand) container.get(PlayerInfoCommand.class.getName()));

getServer().getPluginManager().registerEvents(
    (PlayerLoginListener) container.get(PlayerLoginListener.class.getName()), this);
```

### Named qualifiers

When you have multiple implementations of the same interface:

```java
@Component("primary")
public class PrimaryRepo implements UserRepository { }

@Component("secondary")
public class SecondaryRepo implements UserRepository { }

@Component
public class UserService {
    @Inject("primary")
    public UserRepository repository;
}
```

---

## Error handling

All framework errors throw `InjectionException` (unchecked).

| Situation | Message |
|---|---|
| Component not registered | `"No provider registered for key: ..."` |
| Generated provider class missing | `"Generated provider not found. Make sure annotation processor ran. Expected: ..."` |
| Provider instantiation failure | `"Failed to instantiate provider: ..."` |
| Class not annotated with `@Component` passed to `withComponent` | `"Class is not annotated with @Component: ..."` |

```java
try {
    container.get("com.example.MissingService");
} catch (InjectionException e) {
    // handle
}
```

The most common cause of `"Generated provider not found"` is forgetting to add the `processor` module as an `annotationProcessor` dependency:

```groovy
dependencies {
    implementation project(':runtime')
    annotationProcessor project(':processor')   // required
}
```

---

## Limitations

- **Field injection requires public fields.** The generated provider sets fields directly (`instance.field = ...`), so non-public fields are inaccessible.
- **No circular dependency detection.** A circular dependency will produce a `StackOverflowError` at runtime.
- **Not thread-safe.** `InjectionContainer` uses a plain `HashMap` for the singleton cache. Use external synchronization or build separate containers per thread if needed.
- **One `@Inject` constructor per class.** If multiple constructors are annotated, only the first one found is used.
- **Annotation processor must run.** If you use the `runtime` module without the `processor` as an `annotationProcessor` dependency, the build will compile but `container.get()` will fail at runtime with a missing-provider error.

---

## Design notes

| Pattern | Where used |
|---|---|
| Factory | Each generated `*Provider` class is a factory for its component |
| Builder | `ContainerBuilder` — fluent, immutable-friendly setup |
| Service locator | `InjectionContainer` — central registry looked up by key |
| APT (compile-time metaprogramming) | `InjectionProcessor` generates source files via `javax.annotation.processing` |

Generated providers are placed in a `generated` sub-package (e.g. `com.example.generated.UserServiceProvider`) so they can never collide with a hand-written class named `UserServiceProvider` in `com.example`. Both the processor and the scanner use the constant `GENERATED_SUBPACKAGE = "generated"` — if you ever change it, update both.
