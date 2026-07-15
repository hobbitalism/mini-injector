package com.github.playernguyen.minecraft.command;

import com.github.playernguyen.inject.Component;
import com.github.playernguyen.inject.Inject;
import com.github.playernguyen.minecraft.service.PlayerService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command: /givereward <player_name> <amount>
 * Gives coins to a player.
 */
@Component
public class GiveRewardCommand implements CommandExecutor {
    
    @Inject
    public PlayerService playerService;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /givereward <player_name> <amount>");
            return false;
        }

        String playerName = args[0];
        Player targetPlayer = Bukkit.getPlayer(playerName);

        if (targetPlayer == null) {
            sender.sendMessage("§cPlayer not found: " + playerName);
            return false;
        }

        try {
            long amount = Long.parseLong(args[1]);
            
            if (amount <= 0) {
                sender.sendMessage("§cAmount must be positive!");
                return false;
            }

            playerService.giveCoins(targetPlayer.getUniqueId(), amount);
            
            sender.sendMessage("§a✓ Gave §e" + amount + " §acoins to §e" + playerName);
            targetPlayer.sendMessage("§aYou received §e" + amount + " §acoins!");
            
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount: " + args[1]);
            return false;
        }
    }
}
