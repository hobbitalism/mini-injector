package com.github.playernguyen.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field or constructor parameter for dependency injection.
 * The container will resolve and inject the appropriate dependency.
 */
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
    /**
     * Optional qualifier to specify which implementation to inject
     * when multiple implementations are available.
     */
    String value() default "";
}
