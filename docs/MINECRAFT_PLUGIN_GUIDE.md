# Using MiniInjector with Minecraft Plugins

This guide explains how to use the MiniInjector dependency injection framework in your Spigot/Bukkit Minecraft plugins.

## Table of Contents

1. [Overview](#overview)
2. [Setup](#setup)
3. [Architecture](#architecture)
4. [Example Plugin Walkthrough](#example-plugin-walkthrough)
5. [Best Practices](#best-practices)
6. [Troubleshooting](#troubleshooting)

## Overview

MiniInjector makes it easy to organize your Minecraft plugin code with dependency injection. Instead of manually creating and passing objects around, you annotate your classes and let the framework handle the wiring.

### Benefits for Minecraft Plugins

- **Cleaner Code** - No manual object creation with `new`
- **Testability** - Easy to mock dependencies for testing
- **Maintainability** - Changes to dependencies are centralized
- **Scalability** - Easy to add new features without refactoring
- **Reusability** - Share services across commands, listeners, and tasks

## Setup

### 1. Add Dependencies

In your plugin's `build.gradle`:

```gradle
repositories {
    mavenCentral()
    maven {
        name = "spigot-repo"
        url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
    }
}

dependencies {
    // Spigot API
    compileOnly 'org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT'
    
    // MiniInjector
    implementation 'com.github.playernguyen:miniinjector-core:1.0-SNAPSHOT'
    implementation 'com.github.playernguyen:miniinjector-runtime:1.0-SNAPSHOT'
    annotationProcessor 'com.github.playernguyen:miniinjector-processor:1.0-SNAPSHOT'
}

// Shadow plugin to include MiniInjector in your jar
plugins {
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}

shadowJar {
    archiveClassifier.set('')
    // Relocate to avoid conflicts
    relocate 'com.github.playernguyen.inject', 'yourcompany.inject'
    relocate 'com.github.playernguyen.runtime', 'yourcompany.runtime'
}
```

### 2. Create plugin.yml

```yaml
name: YourPluginName
version: 1.0.0
main: com.example.YourPlugin
description: My awesome plugin with DI

commands:
  mycommand:
    description: My command
    usage: /mycommand
```

### 3. Create the Main Plugin Class

```java
public class YourPlugin extends JavaPlugin {
    private InjectionContainer container;

    @Override
    public void onEnable() {
        initializeContainer();
        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {
        if (container != null) {
            container.clear();
        }
    }

    private void initializeContainer() {
        container = ContainerBuilder.create()
            .withComponent(YourService.class)
            .withComponent(YourCommand.class)
            .build();
    }

    private void registerCommands() {
        YourCommand cmd = (YourCommand) 
            container.get(YourCommand.class.getName());
        getCommand("mycommand").setExecutor(cmd);
    }

    private void registerListeners() {
        YourListener listener = (YourListener) 
            container.get(YourListener.class.getName());
        Bukkit.getPluginManager().registerEvents(listener, this);
    }
}
```

## Architecture

### Layered Structure

A well-organized plugin with DI typically has these layers:

```
┌─────────────────────────────┐
│   Commands & Listeners      │
│  (User interaction points)  │
└──────────────┬──────────────┘
               │ depends on
┌──────────────▼──────────────┐
│   Services                  │
│  (Business logic)           │
└──────────────┬──────────────┘
               │ depends on
┌──────────────▼──────────────┐
│   Repositories              │
│  (Data access)              │
└─────────────────────────────┘
```

### Component Types

#### 1. Repositories (Data Access)
```java
@Component
@Singleton
public class PlayerRepository {
    private final Map<UUID, PlayerData> players = new HashMap<>();
    
    public Optional<PlayerData> load(UUID uuid) {
        return Optional.ofNullable(players.get(uuid));
    }
    
    public void save(PlayerData data) {
        players.put(data.getUuid(), data);
    }
}
```

#### 2. Services (Business Logic)
```java
@Component
@Singleton
public class PlayerService {
    private final PlayerRepository repository;
    
    @Inject
    public PlayerService(PlayerRepository repository) {
        this.repository = repository;
    }
    
    public PlayerData getOrCreate(UUID uuid, String name) {
        return repository.load(uuid)
            .orElseGet(() -> {
                PlayerData newData = new PlayerData(uuid, name);
                repository.save(newData);
                return newData;
            });
    }
}
```

#### 3. Commands (User Interaction)
```java
@Component
public class MyCommand implements CommandExecutor {
    @Inject
    public PlayerService playerService;
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, 
                            String label, String[] args) {
        // Use injected service
        playerService.doSomething();
        return true;
    }
}
```

#### 4. Listeners (Event Handling)
```java
@Component
public class PlayerListener implements Listener {
    @Inject
    public PlayerService playerService;
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        playerService.handleJoin(event.getPlayer());
    }
}
```

## Example Plugin Walkthrough

The `minecraft-plugin` module in this project is a complete working example. Here's what it demonstrates:

### Project Structure

```
minecraft-plugin/
├── src/main/java/com/github/playernguyen/minecraft/
│   ├── MiniInjectorPlugin.java          (Main class)
│   ├── command/
│   │   ├── PlayerInfoCommand.java       (Field injection example)
│   │   └── GiveRewardCommand.java
│   ├── listener/
│   │   └── PlayerLoginListener.java
│   ├── service/
│   │   └── PlayerService.java           (Constructor injection example)
│   ├── repository/
│   │   ├── PlayerRepository.java        (Interface)
│   │   └── InMemoryPlayerRepository.java (@Component, @Singleton)
│   └── model/
│       └── PlayerData.java
└── src/main/resources/
    └── plugin.yml
```

### Key Features Demonstrated

1. **Constructor Injection**
   ```java
   @Component
   public class PlayerService {
       private final PlayerRepository repository;
       
       @Inject
       public PlayerService(PlayerRepository repository) {
           this.repository = repository;
       }
   }
   ```

2. **Field Injection**
   ```java
   @Component
   public class PlayerInfoCommand implements CommandExecutor {
       @Inject
       public PlayerService playerService;
   }
   ```

3. **Singleton Management**
   ```java
   @Component
   @Singleton
   public class InMemoryPlayerRepository implements PlayerRepository {
       // Single instance shared across entire plugin
   }
   ```

4. **Container Initialization**
   ```java
   container = ContainerBuilder.create()
       .withComponent(InMemoryPlayerRepository.class)
       .withComponent(PlayerService.class)
       .withComponent(PlayerInfoCommand.class)
       .build();
   ```

### Running the Example

1. Build the plugin:
   ```bash
   ./gradlew minecraft-plugin:shadowJar
   ```

2. Copy the jar from `minecraft-plugin/build/libs/` to your server's `plugins/` folder

3. Restart the server

4. Use the commands:
   ```
   /playerinfo <name>
   /givereward <name> <amount>
   ```

## Best Practices

### 1. Use Constructor Injection for Services

```java
// ✓ Good - dependencies are required
@Component
public class PlayerService {
    private final PlayerRepository repository;
    
    @Inject
    public PlayerService(PlayerRepository repository) {
        this.repository = repository;
    }
}

// ✗ Avoid - dependencies might be null
@Component
public class PlayerService {
    @Inject
    public PlayerRepository repository;
}
```

### 2. Mark Services as Singletons

```java
// ✓ Good - created once, reused
@Component
@Singleton
public class PlayerService { }

// ✗ Avoid - creates new instance every time
@Component
public class PlayerService { }
```

### 3. Use Interfaces for Repositories

```java
// ✓ Good - easy to swap implementations
@Component
@Singleton
public class PostgresPlayerRepository implements PlayerRepository { }

// In tests:
container.withSingleton(PlayerRepository.class.getName(), mockRepository);

// ✗ Avoid - tightly coupled to implementation
@Component
public class PlayerService {
    @Inject
    public PostgresPlayerRepository repository;
}
```

### 4. Keep Commands Simple

```java
// ✓ Good - command delegates to service
@Component
public class MyCommand implements CommandExecutor {
    @Inject
    public PlayerService playerService;
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, 
                            String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /cmd <arg>");
            return false;
        }
        
        playerService.doSomething(args[0]);
        return true;
    }
}

// ✗ Avoid - business logic in command
@Component
public class MyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, 
                            String label, String[] args) {
        // ... lots of business logic here ...
    }
}
```

### 5. Initialize Container in onEnable

```java
// ✓ Good
@Override
public void onEnable() {
    container = ContainerBuilder.create()
        .withComponent(MyService.class)
        .build();
}

// ✗ Avoid - container not initialized
private InjectionContainer container = new InjectionContainer();
// container is empty when plugin loads!
```

### 6. Clean Up in onDisable

```java
@Override
public void onDisable() {
    if (container != null) {
        container.clear();
    }
}
```

## Troubleshooting

### Issue: "No provider registered for key: ..."

**Cause**: Component not registered in container

**Solution**: Ensure you added it to ContainerBuilder
```java
container = ContainerBuilder.create()
    .withComponent(MissingClass.class)  // Add this
    .build();
```

### Issue: NullPointerException on injected field

**Cause**: Field injection didn't work, field is null

**Solution**: 
1. Check field is public (not private)
2. Check class has @Component annotation
3. Check field has @Inject annotation
4. Check class is registered in container

```java
@Component  // ✓ Required
public class MyCommand implements CommandExecutor {
    @Inject  // ✓ Required
    public PlayerService service;  // ✓ Must be public
    
    // ✗ Not like this:
    // @Inject private PlayerService service;  // Will not be injected!
}
```

### Issue: "Could not resolve all files for configuration"

**Cause**: Missing Spigot repository

**Solution**: Add to build.gradle
```gradle
repositories {
    mavenCentral()
    maven {
        url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
    }
}
```

### Issue: Plugin jar is too large

**Cause**: Dependencies not being shaded

**Solution**: Use shadow plugin
```gradle
plugins {
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}

shadowJar {
    archiveClassifier.set('')
}
```

### Issue: ClassNotFoundException when loading plugin

**Cause**: MiniInjector classes not included in jar

**Solution**: Ensure shadow plugin is configured to include them
```gradle
shadowJar {
    relocate 'com.github.playernguyen', 'yourpackage.shaded'
}
```

## Advanced Topics

### Using Custom Providers

```java
container.withProvider("customService", (c) -> {
    return new MyService(someComplexInitialization());
});

Object service = container.get("customService");
```

### Per-Plugin Instances for Multi-Plugin Setups

```java
// Each plugin instance gets its own container
public class PluginManager {
    private final Map<String, InjectionContainer> containers = new HashMap<>();
    
    public void loadPlugin(String name) {
        InjectionContainer container = ContainerBuilder.create()
            .withComponent(PluginService.class)
            .build();
        containers.put(name, container);
    }
}
```

### Testing with Dependency Injection

```java
@Test
public void testPlayerService() {
    // Mock the repository
    PlayerRepository mockRepo = mock(PlayerRepository.class);
    
    // Create container with mock
    InjectionContainer container = ContainerBuilder.create()
        .withSingleton(PlayerRepository.class.getName(), mockRepo)
        .withComponent(PlayerService.class)
        .build();
    
    // Test
    PlayerService service = (PlayerService) 
        container.get(PlayerService.class.getName());
    // ... assertions ...
}
```

## Summary

MiniInjector brings enterprise-level dependency injection to Minecraft plugins. The example plugin demonstrates:

- ✅ Organized code structure with clear layers
- ✅ Loose coupling between components
- ✅ Easy testing and mocking
- ✅ Zero runtime reflection overhead
- ✅ Type-safe compile-time code generation

Use it to build maintainable, professional-quality plugins!
