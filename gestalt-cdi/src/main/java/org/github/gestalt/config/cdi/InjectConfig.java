package org.github.gestalt.config.cdi;

import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Qualifier;

import java.lang.annotation.*;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
public @interface InjectConfig {
    @Nonbinding
    String path() default "";

    @Nonbinding
    String defaultValue() default "";
}
