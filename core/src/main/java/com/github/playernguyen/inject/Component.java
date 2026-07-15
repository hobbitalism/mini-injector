package com.github.playernguyen.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a component to be managed by the DI container.
 * The component will be registered and available for injection.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
    /**
     * Optional name/qualifier for this component.
     * Useful when multiple implementations of the same interface exist.
     */
    String value() default "";
}
