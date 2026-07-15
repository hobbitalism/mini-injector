package com.github.playernguyen.example;

import com.github.playernguyen.runtime.ContainerBuilder;
import com.github.playernguyen.runtime.InjectionContainer;

/**
 * Demo application showing how to use the dependency injection container.
 */
public class DemoApp {

    public static void main(String[] args) {
        System.out.println("=== MiniInjector Dependency Injection Demo ===\n");

        // Build the injection container with our components
        System.out.println("1. Building injection container...");
        InjectionContainer container = ContainerBuilder.create()
            .withComponent(InMemoryUserRepository.class)
            .withComponent(UserService.class)
            .withComponent(UserController.class)
            .build();
        System.out.println("   ✓ Container built successfully\n");

        // Get the controller from the container
        System.out.println("2. Getting UserController from container...");
        UserController controller = (UserController) container.get(UserController.class.getName());
        System.out.println("   ✓ Controller obtained (with all dependencies injected)\n");

        // Test the injected dependencies
        System.out.println("3. Testing dependency injection...");
        System.out.println("   Getting user with ID 1:");
        String user1 = controller.handleGetUser(1);
        System.out.println("   → " + user1);

        System.out.println("\n   Getting user with ID 2:");
        String user2 = controller.handleGetUser(2);
        System.out.println("   → " + user2);

        // Create a new user
        System.out.println("\n4. Creating a new user...");
        controller.handleCreateUser("100", "Charlie");
        System.out.println("   ✓ User 'Charlie' created");

        System.out.println("\n   Getting newly created user with ID 100:");
        String user100 = controller.handleGetUser(100);
        System.out.println("   → " + user100);

        // Verify singleton behavior
        System.out.println("\n5. Verifying singleton behavior...");
        UserService service1 = (UserService) container.get(UserService.class.getName());
        UserService service2 = (UserService) container.get(UserService.class.getName());
        System.out.println("   Service instances are the same: " + (service1 == service2));

        System.out.println("\n=== Demo completed successfully! ===");
    }
}
