package com.github.playernguyen.runtime;

import com.github.playernguyen.inject.Component;
import com.github.playernguyen.inject.InjectionException;
import com.github.playernguyen.inject.InjectionPoint;

/**
 * Builder for creating and configuring an InjectionContainer.
 * Provides a fluent API for container setup with hybrid approach:
 * - Auto-scan packages for @Component classes
 * - Manual registration for special cases
 * - Custom providers for complex scenarios
 */
public class ContainerBuilder {

    private final InjectionContainer container;
    private final ComponentScanner scanner;

    private ContainerBuilder() {
        this.container = new InjectionContainer();
        this.scanner = new ComponentScanner(container);
    }

    /**
     * Creates a new container builder.
     * 
     * @return a new ContainerBuilder instance
     */
    public static ContainerBuilder create() {
        return new ContainerBuilder();
    }

    /**
     * Scans multiple packages for @Component classes and registers them.
     * This is the recommended approach for most applications.
     * 
     * @param packageNames the packages to scan
     * @return this builder for chaining
     */
    public ContainerBuilder scanPackages(String... packageNames) {
        scanner.scanPackages(packageNames);
        return this;
    }

    /**
     * Scans a single package for components and registers them.
     * 
     * @param packageName the package name to scan
     * @return this builder for chaining
     */
    public ContainerBuilder scanPackage(String packageName) {
        scanner.scanPackage(packageName);
        return this;
    }

    /**
     * Scans the package of a reference class for components.
     * 
     * @param referenceClass a class in the package to scan
     * @return this builder for chaining
     */
    public ContainerBuilder scanPackage(Class<?> referenceClass) {
        scanner.scanPackage(referenceClass);
        return this;
    }

    /**
     * Manually registers a component class with the container.
     * Use this for components in packages that you don't want to scan
     * or when you need fine-grained control.
     * 
     * The class must be annotated with @Component.
     * 
     * @param componentClass the component class to register
     * @return this builder for chaining
     */
    public ContainerBuilder withComponent(Class<?> componentClass) {
        if (!componentClass.isAnnotationPresent(Component.class)) {
            throw new InjectionException("Component class must be annotated with @Component: " 
                + componentClass.getName());
        }
        scanner.registerComponent(componentClass);
        return this;
    }

    /**
     * Registers a custom provider for a given type.
     * Use for non-standard or complex instantiation logic.
     * 
     * @param type the type to register the provider for
     * @param provider the provider instance
     * @return this builder for chaining
     */
    public ContainerBuilder withProvider(Class<?> type, InjectionPoint provider) {
        container.register(type.getName(), provider);
        return this;
    }

    /**
     * Registers a custom provider for a given key.
     * Use for named/qualified bindings.
     * 
     * @param key the key to register the provider under
     * @param provider the provider instance
     * @return this builder for chaining
     */
    public ContainerBuilder withProvider(String key, InjectionPoint provider) {
        container.register(key, provider);
        return this;
    }

    /**
     * Registers a provider that creates a singleton instance.
     * Use for pre-constructed instances (like database connections).
     * 
     * @param type the type
     * @param instance the singleton instance
     * @return this builder for chaining
     */
    public ContainerBuilder withSingleton(Class<?> type, Object instance) {
        return withSingleton(type.getName(), instance);
    }

    /**
     * Registers a provider that creates a singleton instance.
     * Use for pre-constructed instances (like database connections).
     * 
     * @param key the key
     * @param instance the singleton instance
     * @return this builder for chaining
     */
    public ContainerBuilder withSingleton(String key, Object instance) {
        InjectionPoint provider = (c) -> instance;
        container.register(key, provider);
        return this;
    }

    /**
     * Builds and returns the configured InjectionContainer.
     * 
     * @return the configured container
     */
    public InjectionContainer build() {
        return container;
    }
}
