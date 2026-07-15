package com.github.playernguyen.example.minecraft.repository;

import com.github.playernguyen.inject.Component;
import com.github.playernguyen.inject.Singleton;
import com.github.playernguyen.example.minecraft.model.PlayerData;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory implementation of PlayerRepository.
 * Data is stored in memory and lost on server restart.
 * 
 * For production, you would implement a database-backed version.
 */
@Component
@Singleton
public class InMemoryPlayerRepository implements PlayerRepository {
    
    private final Map<UUID, PlayerData> players = new HashMap<>();

    @Override
    public Optional<PlayerData> loadPlayer(UUID uuid) {
        return Optional.ofNullable(players.get(uuid));
    }

    @Override
    public void savePlayer(PlayerData data) {
        players.put(data.getUuid(), data);
    }

    @Override
    public boolean playerExists(UUID uuid) {
        return players.containsKey(uuid);
    }

    @Override
    public void deletePlayer(UUID uuid) {
        players.remove(uuid);
    }

    /**
     * Get all players (for debugging/admin commands).
     */
    public Map<UUID, PlayerData> getAllPlayers() {
        return new HashMap<>(players);
    }
}
