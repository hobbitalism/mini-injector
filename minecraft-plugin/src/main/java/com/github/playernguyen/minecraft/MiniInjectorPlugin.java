package com.github.playernguyen.minecraft;

import com.github.playernguyen.runtime.ContainerBuilder;
import com.github.playernguyen.runtime.InjectionContainer;
import com.github.playernguyen.minecraft.command.GiveRewardCommand;
import com.github.playernguyen.minecraft.command.PlayerInfoCommand;
import com.github.playernguyen.minecraft.listener.PlayerLoginListener;
import com.github.playernguyen.minecraft.repository.InMemoryPlayerRepository;
import com.github.playernguyen.minecraft.service.PlayerService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class demonstrating MiniInjector dependency injection in a Spigot plugin.
 * 
 * This plugin shows:
 * - How to set up DI container in a plugin
 * - Constructor injection (PlayerService <- PlayerRepository)
 * - Field injection (Commands/Listeners <- PlayerService)
 * - Singleton management (@Singleton on repository)
 */
public class MiniInjectorPlugin extends JavaPlugin {
    
    private InjectionContainer container;

    @Override
    public void onEnable() {
        getLogger().info("========================================");
        getLogger().info("MiniInjector Plugin starting...");
        getLogger().info("========================================");

        try {
            // Initialize the DI container
            initializeContainer();
            
            // Register commands
            registerCommands();
            
            // Register event listeners
            registerListeners();
            
            getLogger().info("✓ Plugin enabled successfully!");
            getLogger().info("Commands: /playerinfo, /givereward, /stats");
            getLogger().info("========================================");
            
        } catch (Exception e) {
            getLogger().severe("Failed to initialize plugin!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("MiniInjector Plugin disabled.");
        
        // Clean up DI container
        if (container != null) {
            container.clear();
        }
    }

    /**
     * Initialize the dependency injection container.
     * This registers all components that will be injected.
     */
    private void initializeContainer() {
        getLogger().info("Initializing DI container...");
        
        container = ContainerBuilder.create()
            // Register repository (singleton)
            .withComponent(InMemoryPlayerRepository.class)
            // Register service (uses repository through constructor injection)
            .withComponent(PlayerService.class)
            // Register commands (use service through field injection)
            .withComponent(PlayerInfoCommand.class)
            .withComponent(GiveRewardCommand.class)
            // Register listeners (use service through field injection)
            .withComponent(PlayerLoginListener.class)
            .build();
        
        getLogger().info("✓ DI container initialized with 5 components");
    }

    /**
     * Register all commands with the server.
     */
    private void registerCommands() {
        getLogger().info("Registering commands...");
        
        // Get commands from DI container
        PlayerInfoCommand playerInfoCmd = (PlayerInfoCommand) 
            container.get(PlayerInfoCommand.class.getName());
        GiveRewardCommand giveRewardCmd = (GiveRewardCommand) 
            container.get(GiveRewardCommand.class.getName());
        
        // Register with Bukkit
        getCommand("playerinfo").setExecutor(playerInfoCmd);
        getCommand("givereward").setExecutor(giveRewardCmd);
        
        getLogger().info("✓ Registered 2 commands");
    }

    /**
     * Register all event listeners with the server.
     */
    private void registerListeners() {
        getLogger().info("Registering event listeners...");
        
        // Get listener from DI container
        PlayerLoginListener loginListener = (PlayerLoginListener) 
            container.get(PlayerLoginListener.class.getName());
        
        // Register with Bukkit
        Bukkit.getPluginManager().registerEvents(loginListener, this);
        
        getLogger().info("✓ Registered event listeners");
    }

    /**
     * Get the DI container (for testing or advanced usage).
     */
    public InjectionContainer getContainer() {
        return container;
    }
}
