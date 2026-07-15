package com.github.playernguyen.runtime;

import com.github.playernguyen.inject.InjectionException;
import com.github.playernguyen.inject.InjectionPoint;

import java.util.HashMap;
import java.util.Map;

/**
 * Main dependency injection container.
 * Manages component registration and dependency resolution.
 */
public class InjectionContainer {

    private final Map<String, InjectionPoint> providers = new HashMap<>();
    private final Map<String, Object> singletons = new HashMap<>();

    /**
     * Registers an injection provider for a given key.
     * 
     * @param key the key to register the provider under
     * @param provider the injection provider
     */
    public void register(String key, InjectionPoint provider) {
        providers.put(key, provider);
    }

    /**
     * Gets or creates an instance of a component.
     * 
     * @param key the key of the component
     * @return an instance of the component
     * @throws InjectionException if the component cannot be resolved
     */
    public Object get(String key) {
        if (singletons.containsKey(key)) {
            return singletons.get(key);
        }

        InjectionPoint provider = providers.get(key);
        if (provider == null) {
            throw new InjectionException("No provider registered for key: " + key);
        }

        Object instance = provider.provide(this);
        
        // Cache singletons (in a real implementation, we'd check @Singleton annotation)
        // For now, we cache everything as singleton for simplicity
        singletons.put(key, instance);
        
        return instance;
    }

    /**
     * Gets an instance without caching as singleton.
     * Useful for prototype-scoped beans.
     * 
     * @param key the key of the component
     * @return a new instance of the component
     * @throws InjectionException if the component cannot be resolved
     */
    public Object getPrototype(String key) {
        InjectionPoint provider = providers.get(key);
        if (provider == null) {
            throw new InjectionException("No provider registered for key: " + key);
        }
        return provider.provide(this);
    }

    /**
     * Checks if a component is registered.
     * 
     * @param key the key to check
     * @return true if registered, false otherwise
     */
    public boolean isRegistered(String key) {
        return providers.containsKey(key);
    }

    /**
     * Clears all cached singleton instances.
     * Useful for testing or resetting the container.
     */
    public void clearSingletons() {
        singletons.clear();
    }

    /**
     * Clears all providers and singletons.
     * Resets the container to initial state.
     */
    public void clear() {
        providers.clear();
        singletons.clear();
    }
}
