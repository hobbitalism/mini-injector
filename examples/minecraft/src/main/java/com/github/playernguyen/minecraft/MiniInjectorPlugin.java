package com.github.playernguyen.minecraft;

import com.github.playernguyen.minecraft.command.GiveRewardCommand;
import com.github.playernguyen.minecraft.command.PlayerInfoCommand;
import com.github.playernguyen.minecraft.listener.PlayerLoginListener;
import com.github.playernguyen.runtime.ContainerBuilder;
import com.github.playernguyen.runtime.InjectionContainer;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point for the MiniInjector Spigot plugin example.
 *
 * <p>Demonstrates how to use the MiniInjector DI framework inside a Bukkit/Spigot plugin:
 * <ul>
 *   <li>Auto-scanning {@code @Component} classes in the plugin package</li>
 *   <li>Registering the plugin itself as a singleton so components can inject it</li>
 *   <li>Wiring command executors and event listeners obtained from the container</li>
 * </ul>
 */
public class MiniInjectorPlugin extends JavaPlugin {

    private InjectionContainer container;

    @Override
    public void onEnable() {
        getLogger().info("Building injection container...");

        container = ContainerBuilder.create()
                // Make the plugin instance itself available for injection before scanning,
                // so components that depend on MiniInjectorPlugin can receive it.
                .withSingleton(MiniInjectorPlugin.class.getName(), this)
                // Auto-scan all @Component-annotated classes in this plugin's root package
                .scanPackage("com.github.playernguyen.minecraft")
                .build();

        getLogger().info("Container built — registering commands and listeners...");

        registerCommands();
        registerListeners();

        getLogger().info("MiniInjectorPlugin enabled successfully.");
    }

    @Override
    public void onDisable() {
        getLogger().info("MiniInjectorPlugin disabled.");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void registerCommands() {
        PlayerInfoCommand playerInfoCommand =
                (PlayerInfoCommand) container.get(PlayerInfoCommand.class.getName());
        GiveRewardCommand giveRewardCommand =
                (GiveRewardCommand) container.get(GiveRewardCommand.class.getName());

        getCommand("playerinfo").setExecutor(playerInfoCommand);
        getCommand("givereward").setExecutor(giveRewardCommand);
    }

    private void registerListeners() {
        PlayerLoginListener playerLoginListener =
                (PlayerLoginListener) container.get(PlayerLoginListener.class.getName());

        getServer().getPluginManager().registerEvents(playerLoginListener, this);
    }

    /**
     * Exposes the DI container so other plugin classes can resolve dependencies at runtime
     * if needed (e.g. from a command that creates objects dynamically).
     *
     * @return the plugin's {@link InjectionContainer}
     */
    public InjectionContainer getContainer() {
        return container;
    }
}
