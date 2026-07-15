package com.github.playernguyen.example;

import com.github.playernguyen.runtime.ContainerBuilder;
import com.github.playernguyen.runtime.InjectionContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the dependency injection container.
 */
public class DIContainerTest {

    private InjectionContainer container;

    @BeforeEach
    public void setUp() {
        container = ContainerBuilder.create()
            .withComponent(InMemoryUserRepository.class)
            .withComponent(UserService.class)
            .withComponent(UserController.class)
            .build();
    }

    @Test
    public void testRepositoryComponentIsRegistered() {
        assertTrue(container.isRegistered(UserRepository.class.getName()));
        assertTrue(container.isRegistered(InMemoryUserRepository.class.getName()));
    }

    @Test
    public void testServiceComponentIsRegistered() {
        assertTrue(container.isRegistered(UserService.class.getName()));
    }

    @Test
    public void testControllerComponentIsRegistered() {
        assertTrue(container.isRegistered(UserController.class.getName()));
    }

    @Test
    public void testGetRepositoryFromContainer() {
        Object repo = container.get(UserRepository.class.getName());
        assertNotNull(repo);
        assertInstanceOf(UserRepository.class, repo);
    }

    @Test
    public void testGetServiceFromContainer() {
        Object service = container.get(UserService.class.getName());
        assertNotNull(service);
        assertInstanceOf(UserService.class, service);
    }

    @Test
    public void testGetControllerFromContainer() {
        Object controller = container.get(UserController.class.getName());
        assertNotNull(controller);
        assertInstanceOf(UserController.class, controller);
    }

    @Test
    public void testSingletonBehavior() {
        Object service1 = container.get(UserService.class.getName());
        Object service2 = container.get(UserService.class.getName());
        assertSame(service1, service2);
    }

    @Test
    public void testInjectedDependencies() {
        UserService service = (UserService) container.get(UserService.class.getName());
        assertNotNull(service);
        
        // Test that dependencies were properly injected
        String user = service.getUser(1);
        assertEquals("Alice", user);
    }

    @Test
    public void testControllerWithInjectedDependencies() {
        UserController controller = (UserController) container.get(UserController.class.getName());
        assertNotNull(controller);
        
        // Test that the service was injected into the controller
        String result = controller.handleGetUser(1);
        assertTrue(result.contains("Alice"));
    }

    @Test
    public void testDependencyChain() {
        // This test verifies the entire dependency chain works
        UserController controller = (UserController) container.get(UserController.class.getName());
        
        controller.handleCreateUser("99", "TestUser");
        String result = controller.handleGetUser(99);
        
        assertTrue(result.contains("TestUser"));
    }

    @Test
    public void testUnregisteredComponentThrowsException() {
        assertThrows(com.github.playernguyen.inject.InjectionException.class, () -> {
            container.get("com.github.playernguyen.example.NonExistentClass");
        });
    }
}
