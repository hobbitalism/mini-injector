package com.github.playernguyen.example.demoapp;

import com.github.playernguyen.inject.Component;
import com.github.playernguyen.inject.Inject;
import com.github.playernguyen.inject.Singleton;

/**
 * Service layer for user operations.
 * Demonstrates constructor-based dependency injection.
 */
@Component
@Singleton
public class UserService {
    
    private final UserRepository userRepository;

    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getUser(int id) {
        return userRepository.getUserById(id);
    }

    public void createUser(String id, String name) {
        userRepository.saveUser(id, name);
    }
}
