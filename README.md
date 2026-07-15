# MiniInjector - Lightweight Dependency Injection Framework

A lightweight, compile-time annotation processor-based dependency injection framework for Java. MiniInjector generates type-safe injection providers at compile-time, eliminating runtime reflection overhead and providing excellent performance.

## Features

✅ **Compile-Time Code Generation** - Annotation processor generates injection providers during compilation  
✅ **Zero Runtime Reflection** - Type-safe, generated code with no reflection overhead  
✅ **Constructor & Field Injection** - Support for both injection strategies  
✅ **Singleton Management** - Automatic singleton caching with `@Singleton` annotation  
✅ **Qualifier Support** - Handle multiple implementations with named qualifiers  
✅ **Fluent API** - Easy container configuration with `ContainerBuilder`  
✅ **Minimal Dependencies** - Only requires Java compiler API  
✅ **Full Test Coverage** - Integration tests demonstrating all features  

## Project Structure

```
MiniInjector/
├── core/                          # Core annotations and interfaces
│   └── src/main/java/
│       └── com/github/playernguyen/inject/
│           ├── @Component         # Component marker annotation
│           ├── @Inject            # Injection point annotation
│           ├── @Singleton         # Singleton scope annotation
│           ├── InjectionPoint     # Provider interface
│           └── InjectionException # Exception type
│
├── processor/                     # Annotation processor (compile-time)
│   └── src/main/java/
│       └── com/github/playernguyen/processor/
│           └── InjectionProcessor # Generates *Provider classes
│
├── runtime/                       # Runtime DI container
│   └── src/main/java/
│       └── com/github/playernguyen/runtime/
│           ├── InjectionContainer # Main DI container
│           ├── ComponentScanner    # Component discovery
│           └── ContainerBuilder    # Fluent configuration API
│
└── example/                       # Example usage and tests
    ├── src/main/java/
    │   └── com/github/playernguyen/example/
    │       ├── UserRepository      # Interface
    │       ├── InMemoryUserRepository  # Implementation
    │       ├── UserService         # Service layer
    │       ├── UserController      # Controller layer
    │       └── DemoApp             # Demo application
    │
    └── src/test/java/
        └── DIContainerTest.java    # Integration tests
```

## Quick Start

### 1. Mark Components

```java
@Component
@Singleton
public class UserRepository {
    // Repository implementation
}
```

### 2. Inject Dependencies

**Constructor Injection:**
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

**Field Injection:**
```java
@Component
public class UserController {
    @Inject
    public UserService userService;
}
```

### 3. Build and Use Container

```java
InjectionContainer container = ContainerBuilder.create()
    .withComponent(UserRepository.class)
    .withComponent(UserService.class)
    .withComponent(UserController.class)
    .build();

UserController controller = (UserController) 
    container.get(UserController.class.getName());
```

## How It Works

### Compilation Phase
```
Source Code (.java)
    ↓
InjectionProcessor scans for @Component classes
    ↓
Generates *Provider classes that implement InjectionPoint
    ↓
Provider classes are compiled with your code
```

### Runtime Phase
```
ContainerBuilder registers components
    ↓
container.get(key) looks up provider
    ↓
Provider.provide(container) creates instance
    ↓
Constructor dependencies injected
    ↓
Field dependencies injected
    ↓
Fully initialized instance returned
```

## Annotation Guide

### @Component
Marks a class as a manageable component:
```java
@Component                    // Uses class name as key
public class MyService { }

@Component("myCustomName")    // Custom name/qualifier
public class MyService { }
```

### @Inject
Marks injection points:
```java
@Inject
public MyService(Repository repo) { }  // Constructor injection

@Inject
public Repository repository;           // Field injection (must be public)

@Inject("qualifierName")
public Repository repo;                 // With qualifier
```

### @Singleton
Marks components as singletons (cached):
```java
@Component
@Singleton
public class DatabaseConnection {
    // Only one instance per container
}
```

## API Reference

### InjectionContainer

```java
// Register a provider
container.register(String key, InjectionPoint provider);

// Get cached instance (singleton)
Object instance = container.get(String key);

// Get new instance (prototype)
Object newInstance = container.getPrototype(String key);

// Check if registered
boolean registered = container.isRegistered(String key);

// Clear singletons
container.clearSingletons();

// Clear all
container.clear();
```

### ContainerBuilder

```java
// Create builder
ContainerBuilder builder = ContainerBuilder.create();

// Register component
builder.withComponent(MyComponent.class);

// Register custom provider
builder.withProvider(String key, InjectionPoint provider);

// Register singleton instance
builder.withSingleton(String key, Object instance);

// Scan package
builder.scanPackage("com.example.components");

// Build container
InjectionContainer container = builder.build();
```

## Examples

### Basic Example
See `example/src/main/java/com/github/playernguyen/example/DemoApp.java`

Run with:
```bash
./gradlew build
java -cp "example/build/classes/java/main:runtime/build/classes/java/main:core/build/classes/java/main" \
    com.github.playernguyen.example.DemoApp
```

### Running Tests
```bash
./gradlew test
```

### Using Qualifiers
```java
@Component("primary")
public class PrimaryRepository implements UserRepository { }

@Component("secondary")
public class SecondaryRepository implements UserRepository { }

public class UserService {
    @Inject("primary")
    public UserRepository repository;
}
```

## Building the Project

### Prerequisites
- Java 11+
- Gradle 9.3.0+

### Build Commands
```bash
# Build all modules
./gradlew build

# Run tests
./gradlew test

# Clean build
./gradlew clean build

# View dependencies
./gradlew dependencies
```

## Design Patterns

- **Factory Pattern** - Generated `*Provider` classes act as factories
- **Builder Pattern** - `ContainerBuilder` for fluent configuration
- **Service Locator** - `InjectionContainer` as service registry
- **Annotation Processing** - Compile-time code generation

## Performance Characteristics

- **Compilation Overhead** - Minimal, only annotation processing
- **Runtime Overhead** - Zero reflection, pure Java method calls
- **Memory Usage** - Lightweight provider objects + cached singletons
- **Singleton Caching** - O(1) lookup for cached instances

## Error Handling

The framework throws `InjectionException` when:
- A requested component is not registered
- A generated provider class cannot be found
- An error occurs during instance creation

Example:
```java
try {
    Object instance = container.get("unknown.Component");
} catch (InjectionException e) {
    System.err.println("Component not found: " + e.getMessage());
}
```

## Limitations

- No automatic classpath scanning (must manually register components)
- No circular dependency detection
- Field injection requires public fields
- Not thread-safe (design for single-threaded use or external synchronization)

## Future Enhancements

- [ ] Automatic package scanning with `@ComponentScan`
- [ ] Circular dependency detection
- [ ] Named bindings with custom qualifiers
- [ ] Optional and lazy injection support
- [ ] Post-construct lifecycle callbacks (`@PostConstruct`)
- [ ] Pre-destroy callbacks (`@PreDestroy`)
- [ ] Interceptor/AOP support
- [ ] Thread-safe container options
- [ ] Configuration property injection

## Documentation

See `docs/ARCHITECTURE.md` for detailed architecture documentation.

## License

This is a demonstration project for understanding dependency injection patterns and annotation processing.

## Author

Created as an example of building a lightweight DI framework using Java's annotation processing API.
