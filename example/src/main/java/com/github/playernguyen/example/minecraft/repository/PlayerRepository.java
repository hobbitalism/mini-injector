package com.github.playernguyen.example.minecraft.repository;

import com.github.playernguyen.example.minecraft.model.PlayerData;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for player data persistence.
 */
public interface PlayerRepository {
    /**
     * Load player data by UUID.
     */
    Optional<PlayerData> loadPlayer(UUID uuid);

    /**
     * Save player data.
     */
    void savePlayer(PlayerData data);

    /**
     * Check if player exists.
     */
    boolean playerExists(UUID uuid);

    /**
     * Delete player data.
     */
    void deletePlayer(UUID uuid);
}
