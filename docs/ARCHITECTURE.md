# MiniInjector Architecture

## Overview

MiniInjector is a lightweight dependency injection (DI) framework for Java that uses compile-time annotation processing to generate injection providers. This document describes the architecture and design of the system.

## Module Structure

The project is organized into four main modules:

### 1. Core Module (`core/`)
Contains the annotation definitions and core interfaces.

**Key Classes:**
- `@Component` - Marks a class as a DI component
- `@Inject` - Marks fields or constructor parameters for injection
- `@Singleton` - Marks components as singletons (cached)
- `InjectionPoint` - Interface for generated providers
- `InjectionException` - Exception for DI failures

**Location:** `core/src/main/java/com/github/playernguyen/inject/`

### 2. Processor Module (`processor/`)
Contains the annotation processor that runs at compile-time to generate injection providers.

**Key Classes:**
- `InjectionProcessor` - Main annotation processor that generates provider classes

**How It Works:**
1. Scans for classes annotated with `@Component`
2. Analyzes constructor and field dependencies marked with `@Inject`
3. Generates a `{ClassName}Provider` class that implements `InjectionPoint`
4. The generated provider handles creating instances with all dependencies injected

**Generated Code Example:**
```java
// For a component class: public class UserService { ... }
// Generates: UserServiceProvider.java

public class UserServiceProvider implements InjectionPoint {
    @Override
    public Object provide(Object container) {
        InjectionContainer injectionContainer = (InjectionContainer) container;
        UserService instance = new UserService(
            (UserRepository) injectionContainer.get("com.github.playernguyen.example.UserRepository")
        );
        // ... field injection code ...
        return instance;
    }
}
```

**Location:** `processor/src/main/java/com/github/playernguyen/processor/`

### 3. Runtime Module (`runtime/`)
Contains the runtime DI container and component scanner.

**Key Classes:**
- `InjectionContainer` - The main DI container that manages component registration and dependency resolution
- `ComponentScanner` - Discovers and registers components
- `ContainerBuilder` - Fluent API for building and configuring the container

**Container Responsibilities:**
- Register providers for components
- Resolve dependencies at runtime
- Cache singleton instances
- Provide access to registered components

**Location:** `runtime/src/main/java/com/github/playernguyen/runtime/`

### 4. Example Module (`example/`)
Demonstrates how to use the DI framework with example components.

**Example Components:**
- `UserRepository` - Interface for data access
- `InMemoryUserRepository` - Implementation of UserRepository
- `UserService` - Service layer with constructor injection
- `UserController` - Controller with field injection

**Location:** `example/src/main/java/com/github/playernguyen/example/`

## Dependency Injection Flow

### 1. Compile-Time (Annotation Processing)
```
Source Code (.java)
        ↓
  Javac + InjectionProcessor
        ↓
  Generated Providers (*Provider.java)
        ↓
Compiled Classes (.class)
```

### 2. Runtime (Dependency Resolution)
```
Application starts
        ↓
Create InjectionContainer
        ↓
Register Components (via ContainerBuilder)
        ↓
Request Component (container.get(key))
        ↓
Invoke *Provider.provide(container)
        ↓
Constructor Injection
        ↓
Field Injection
        ↓
Return Fully Initialized Instance
```

## Injection Types Supported

### Constructor Injection
Mark a constructor with `@Inject` to inject dependencies through constructor parameters:

```java
@Component
public class UserService {
    private final UserRepository repository;

    @Inject
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

### Field Injection
Mark fields with `@Inject` to inject dependencies after object creation:

```java
@Component
public class UserController {
    @Inject
    public UserRepository repository;  // Must be public
}
```

## Qualifier Support

When multiple implementations of an interface exist, use the `value()` parameter of `@Inject` and `@Component` to disambiguate:

```java
@Component("primary")
public class PrimaryRepository implements UserRepository { }

@Component("secondary")
public class SecondaryRepository implements UserRepository { }

@Component
public class UserService {
    @Inject("primary")
    public UserRepository repository;
}
```

## Lifecycle Management

### Singleton Scope
Components marked with `@Singleton` are instantiated once and cached:

```java
@Component
@Singleton
public class DatabaseConnection {
    // Only one instance created for entire application
}
```

### Prototype Scope
Components without `@Singleton` can be retrieved fresh using `getPrototype()`:

```java
Object newInstance = container.getPrototype(SomeComponent.class.getName());
```

## Error Handling

The framework throws `InjectionException` when:
- A required component is not registered
- A provider class cannot be found (annotation processor didn't run)
- An exception occurs during instantiation

## Building and Using

### Building the Project
```bash
./gradlew build
```

### Running the Example
```bash
./gradlew example:test
```

### Using in Your Project

1. Add runtime dependency:
```gradle
dependencies {
    implementation project(':runtime')
    annotationProcessor project(':processor')
}
```

2. Mark components:
```java
@Component
@Singleton
public class MyService {
    // implementation
}
```

3. Create and use the container:
```java
InjectionContainer container = ContainerBuilder.create()
    .withComponent(MyService.class)
    .build();

MyService service = (MyService) container.get(MyService.class.getName());
```

## Design Patterns

### Builder Pattern
`ContainerBuilder` provides a fluent API for configuring the container.

### Factory Pattern
Generated `*Provider` classes act as factories for creating component instances.

### Service Locator Pattern
`InjectionContainer` acts as a service locator for retrieving registered components.

## Performance Considerations

1. **Compile-Time Processing** - Annotation processor runs during compilation, zero runtime cost
2. **Singleton Caching** - Components marked with `@Singleton` are cached, reducing object creation
3. **No Reflection at Runtime** - Generated code is type-safe with no reflection overhead
4. **Minimal Runtime Footprint** - Only `runtime` and `core` modules needed at runtime

## Thread Safety

The current implementation is not thread-safe. For multi-threaded applications:
- Create separate container instances per thread, or
- Synchronize access to the container, or
- Extend the implementation with thread-safe caching

## Limitations and Future Improvements

Current Limitations:
- No automatic package scanning (must manually register components)
- No circular dependency detection
- Field injection requires public fields
- No named bindings with custom strings

Potential Enhancements:
- Automatic classpath scanning for components
- Circular dependency detection and reporting
- Support for optional dependencies
- Post-construct lifecycle callbacks
- Interceptor/AOP support
