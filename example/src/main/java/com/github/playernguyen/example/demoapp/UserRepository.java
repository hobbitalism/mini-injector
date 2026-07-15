package com.github.playernguyen.example.demoapp;

/**
 * Interface for user repository operations.
 */
public interface UserRepository {
    String getUserById(int id);
    void saveUser(String id, String name);
}
