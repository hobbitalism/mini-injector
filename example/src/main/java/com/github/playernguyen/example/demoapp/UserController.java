package com.github.playernguyen.example.demoapp;

import com.github.playernguyen.inject.Component;
import com.github.playernguyen.inject.Inject;

/**
 * Controller layer demonstrating field-based dependency injection.
 */
@Component
public class UserController {
    
    @Inject
    public UserService userService;

    public String handleGetUser(int userId) {
        return "User: " + userService.getUser(userId);
    }

    public void handleCreateUser(String id, String name) {
        userService.createUser(id, name);
    }
}
