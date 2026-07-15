package com.github.playernguyen.example.minecraft.model;

import java.util.UUID;

/**
 * Represents player data stored in the system.
 */
public class PlayerData {
    private final UUID uuid;
    private final String name;
    private long coins;
    private int level;
    private long lastLogin;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.coins = 0;
        this.level = 1;
        this.lastLogin = System.currentTimeMillis();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public long getCoins() {
        return coins;
    }

    public void addCoins(long amount) {
        this.coins += amount;
    }

    public void removeCoins(long amount) {
        this.coins = Math.max(0, this.coins - amount);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public void addLevel(int levels) {
        this.level += levels;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "name='" + name + '\'' +
                ", coins=" + coins +
                ", level=" + level +
                '}';
    }
}
