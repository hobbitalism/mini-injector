# MiniInjector Examples

This directory contains two example projects demonstrating how to use the MiniInjector dependency injection framework:

## 1. DemoApp - Simple Console Example

**Location:** `examples/demoapp/`

A simple console application demonstrating the basic features of MiniInjector:

- Component registration with `@Component`
- Constructor-based dependency injection
- Field-based dependency injection  
- Singleton management with `@Singleton`
- Container building with `ContainerBuilder`

### Running the Demo App

```bash
# Build the project
./gradlew build

# Run the demo
java -cp "examples/demoapp/build/classes/java/main;runtime/build/classes/java/main;core/build/classes/java/main;processor/build/classes/java/main;processor/build/resources/main" com.github.playernguyen.demoapp.DemoApp
```

### What It Shows

- **UserRepository** - Interface for data access
- **InMemoryUserRepository** - Implementation with `@Component` and `@Singleton`
- **UserService** - Business logic with constructor injection
- **UserController** - Controller with field injection
- **DemoApp** - Main class that builds container and demonstrates all features

---

## 2. Minecraft - Spigot Plugin Example

**Location:** `examples/minecraft/`

A complete, real-world Spigot Minecraft plugin example showing how to use MiniInjector in production code:

- Layered architecture (model, repository, service, command, listener)
- Constructor and field injection in game events
- Integration with Bukkit/Spigot API

### Building the Plugin

```bash
# Build the plugin
./gradlew examples:minecraft:build

# Plugin jar is created at: examples/minecraft/build/libs/minecraft.jar
```

### Deploying to a Server

1. Build the plugin with the command above
2. Copy `examples/minecraft/build/libs/minecraft.jar` to your server's `plugins/` folder
3. Restart the server
4. Use the commands: `/playerinfo`, `/givereward`

### What It Shows

- **PlayerData** - Model class representing player state
- **PlayerRepository** - Interface with implementations
- **InMemoryPlayerRepository** - Data persistence with DI
- **PlayerService** - Business logic with constructor injection
- **PlayerInfoCommand** - Command handler with field injection
- **GiveRewardCommand** - Another command with injected service
- **PlayerLoginListener** - Event handler demonstrating field injection
- **MiniInjectorPlugin** - Main plugin class with container setup

---

## Project Structure

```
examples/
├── README.md                          (This file)
├── build.gradle                       (Parent config)
├── 
├── demoapp/                           (Simple example)
│   ├── build.gradle
│   └── src/main/java/com/github/playernguyen/demoapp/
│       ├── DemoApp.java
│       ├── UserRepository.java
│       ├── InMemoryUserRepository.java
│       ├── UserService.java
│       └── UserController.java
│
└── minecraft/                         (Real-world Minecraft plugin)
    ├── build.gradle
    ├── src/main/java/com/github/playernguyen/minecraft/
    │   ├── command/
    │   │   ├── PlayerInfoCommand.java
    │   │   └── GiveRewardCommand.java
    │   ├── listener/
    │   │   └── PlayerLoginListener.java
    │   ├── service/
    │   │   └── PlayerService.java
    │   ├── repository/
    │   │   ├── PlayerRepository.java
    │   │   └── InMemoryPlayerRepository.java
    │   └── model/
    │       └── PlayerData.java
    │
    └── src/main/resources/
        └── plugin.yml
```

---

## Key Differences Between Examples

| Feature | DemoApp | Minecraft |
|---------|---------|-----------|
| Purpose | Learning/Demo | Production Plugin |
| Dependencies | None (except MiniInjector) | Spigot API |
| Entry Point | `main()` method | `JavaPlugin.onEnable()` |
| Use Case | Console application | Game server plugin |
| Complexity | Beginner | Intermediate/Advanced |
| Real-world | No | Yes |

---

## Learning Path

1. **Start with DemoApp** if you're new to MiniInjector
2. **Progress to Minecraft** to see real-world usage patterns
3. **Refer to docs/ARCHITECTURE.md** for deeper technical details
4. **Check MINECRAFT_PLUGIN_GUIDE.md** for plugin development best practices

---

## Further Reading

- `docs/ARCHITECTURE.md` - Technical architecture details
- `docs/MINECRAFT_PLUGIN_GUIDE.md` - Minecraft plugin development guide
- `README.md` (root) - Main project documentation
