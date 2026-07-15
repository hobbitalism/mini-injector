package com.github.playernguyen.minecraft.listener;

import com.github.playernguyen.inject.Component;
import com.github.playernguyen.inject.Inject;
import com.github.playernguyen.minecraft.service.PlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listens to player login/logout events.
 * Demonstrates field-based dependency injection.
 */
@Component
public class PlayerLoginListener implements Listener {
    
    @Inject
    public PlayerService playerService;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerService.getOrCreatePlayer(
            event.getPlayer().getUniqueId(),
            event.getPlayer().getName()
        );
        playerService.updateLastLogin(event.getPlayer().getUniqueId());
        
        event.getPlayer().sendMessage("§aWelcome! Use §e/playerinfo <name> §ato check stats.");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Could save player data to database here
    }
}
