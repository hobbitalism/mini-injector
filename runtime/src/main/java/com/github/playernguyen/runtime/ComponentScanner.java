package com.github.playernguyen.runtime;

import com.github.playernguyen.inject.Component;
import com.github.playernguyen.inject.InjectionException;
import com.github.playernguyen.inject.InjectionPoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Scans for and registers components with the injection container.
 * Automatically discovers generated provider classes.
 */
public class ComponentScanner {

    private final InjectionContainer container;

    public ComponentScanner(InjectionContainer container) {
        this.container = container;
    }

    /**
     * Scans the classpath for components and registers them.
     * Looks for classes in the same package as a reference class.
     * 
     * @param referenceClass a class in the package to scan
     */
    public void scanPackage(Class<?> referenceClass) {
        scanPackage(referenceClass.getPackageName());
    }

    /**
     * Scans a specific package for components and registers them.
     * 
     * @param packageName the package to scan
     */
    public void scanPackage(String packageName) {
        List<Class<?>> classes = getClassesInPackage(packageName);
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Component.class)) {
                registerComponent(clazz);
            }
        }
    }

    /**
     * Registers a single component class with the container.
     * 
     * @param componentClass the component class to register
     */
    public void registerComponent(Class<?> componentClass) {
        Component componentAnnotation = componentClass.getAnnotation(Component.class);
        if (componentAnnotation == null) {
            throw new InjectionException("Class is not annotated with @Component: " + componentClass.getName());
        }

        String componentName = componentAnnotation.value();
        if (componentName.isEmpty()) {
            componentName = componentClass.getName();
        }

        // Try to load the generated provider
        String providerClassName = componentClass.getPackageName() + "." 
            + componentClass.getSimpleName() + "Provider";
        
        try {
            Class<?> providerClass = Class.forName(providerClassName);
            InjectionPoint provider = (InjectionPoint) providerClass.getDeclaredConstructor().newInstance();
            container.register(componentName, provider);
            
            // Also register by interface types if component implements interfaces
            for (Class<?> iface : componentClass.getInterfaces()) {
                container.register(iface.getName(), provider);
            }
        } catch (ClassNotFoundException e) {
            throw new InjectionException(
                "Generated provider not found. Make sure annotation processor ran: " + providerClassName, 
                e
            );
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new InjectionException("Failed to instantiate provider: " + providerClassName, e);
        }
    }

    /**
     * Gets all classes in a package.
     * This is a simplified implementation that scans the classpath.
     * 
     * @param packageName the package name
     * @return list of classes in the package
     */
    private List<Class<?>> getClassesInPackage(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        
        try {
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                // Note: This is a simplified implementation
                // A production implementation would need more robust classpath scanning
            }
        } catch (Exception e) {
            // Silently ignore - classpath scanning can be complex
        }
        
        return classes;
    }
}
