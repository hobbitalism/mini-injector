package com.github.playernguyen.example.minecraft.service;

import com.github.playernguyen.inject.Component;
import com.github.playernguyen.inject.Inject;
import com.github.playernguyen.inject.Singleton;
import com.github.playernguyen.example.minecraft.model.PlayerData;
import com.github.playernguyen.example.minecraft.repository.PlayerRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for player operations.
 * Demonstrates constructor-based dependency injection with PlayerRepository.
 */
@Component
@Singleton
public class PlayerService {
    
    private final PlayerRepository playerRepository;

    @Inject
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /**
     * Get or create a player.
     */
    public PlayerData getOrCreatePlayer(UUID uuid, String name) {
        Optional<PlayerData> existing = playerRepository.loadPlayer(uuid);
        
        if (existing.isPresent()) {
            return existing.get();
        }

        PlayerData newPlayer = new PlayerData(uuid, name);
        playerRepository.savePlayer(newPlayer);
        return newPlayer;
    }

    /**
     * Get player by UUID.
     */
    public Optional<PlayerData> getPlayer(UUID uuid) {
        return playerRepository.loadPlayer(uuid);
    }

    /**
     * Give coins to a player.
     */
    public void giveCoins(UUID uuid, long amount) {
        Optional<PlayerData> player = playerRepository.loadPlayer(uuid);
        
        if (player.isPresent()) {
            PlayerData data = player.get();
            data.addCoins(amount);
            playerRepository.savePlayer(data);
        }
    }

    /**
     * Remove coins from a player.
     */
    public void removeCoins(UUID uuid, long amount) {
        Optional<PlayerData> player = playerRepository.loadPlayer(uuid);
        
        if (player.isPresent()) {
            PlayerData data = player.get();
            data.removeCoins(amount);
            playerRepository.savePlayer(data);
        }
    }

    /**
     * Level up a player.
     */
    public void levelUp(UUID uuid) {
        Optional<PlayerData> player = playerRepository.loadPlayer(uuid);
        
        if (player.isPresent()) {
            PlayerData data = player.get();
            data.addLevel(1);
            playerRepository.savePlayer(data);
        }
    }

    /**
     * Update last login timestamp.
     */
    public void updateLastLogin(UUID uuid) {
        Optional<PlayerData> player = playerRepository.loadPlayer(uuid);
        
        if (player.isPresent()) {
            PlayerData data = player.get();
            data.setLastLogin(System.currentTimeMillis());
            playerRepository.savePlayer(data);
        }
    }
}
