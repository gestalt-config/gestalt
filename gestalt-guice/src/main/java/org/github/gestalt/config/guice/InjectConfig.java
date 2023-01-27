package org.github.gestalt.config.guice;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to mark an object as a configuration for injection.
 * Must provide a path to the configuration.
 * Only supports fields. (limit of Guice)
 *
 * @author <a href="mailto:colin.redmond@outlook.com">Colin Redmond (c) 2023.
 */
@Target(value = {FIELD})
@Inherited
@Retention(value = RUNTIME)
public @interface InjectConfig {
    String path();
}
