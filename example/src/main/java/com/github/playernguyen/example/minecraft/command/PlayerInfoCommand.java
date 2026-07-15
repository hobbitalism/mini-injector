package com.github.playernguyen.example.minecraft.command;

import com.github.playernguyen.inject.Component;
import com.github.playernguyen.inject.Inject;
import com.github.playernguyen.example.minecraft.model.PlayerData;
import com.github.playernguyen.example.minecraft.service.PlayerService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Command: /playerinfo <player_name>
 * Shows information about a player.
 */
@Component
public class PlayerInfoCommand implements CommandExecutor {
    
    @Inject
    public PlayerService playerService;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /playerinfo <player_name>");
            return false;
        }

        String playerName = args[0];
        Player targetPlayer = Bukkit.getPlayer(playerName);

        if (targetPlayer == null) {
            sender.sendMessage("§cPlayer not found: " + playerName);
            return false;
        }

        Optional<PlayerData> playerData = playerService.getPlayer(targetPlayer.getUniqueId());

        if (playerData.isEmpty()) {
            sender.sendMessage("§cNo data for player: " + playerName);
            return false;
        }

        PlayerData data = playerData.get();
        sender.sendMessage("§6=== Player Info: " + data.getName() + " ===");
        sender.sendMessage("§eCoins: §a" + data.getCoins());
        sender.sendMessage("§eLevel: §a" + data.getLevel());
        sender.sendMessage("§eUUID: §a" + data.getUuid());
        
        return true;
    }
}
