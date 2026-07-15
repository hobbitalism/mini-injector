package com.github.playernguyen.example;

import com.github.playernguyen.inject.Component;
import com.github.playernguyen.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of UserRepository.
 * Marked as a component for dependency injection.
 */
@Component
@Singleton
public class InMemoryUserRepository implements UserRepository {
    
    private final Map<String, String> users = new HashMap<>();

    public InMemoryUserRepository() {
        // Sample data
        users.put("1", "Alice");
        users.put("2", "Bob");
    }

    @Override
    public String getUserById(int id) {
        return users.getOrDefault(String.valueOf(id), "Unknown");
    }

    @Override
    public void saveUser(String id, String name) {
        users.put(id, name);
    }
}
