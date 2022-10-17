package org.github.gestalt.config.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A annotation to apply a priority to a configuration.
 */
@Target(value = {TYPE})
@Retention(value = RUNTIME)
public @interface ConfigPriority {

    /**
     * The priority of the Config.
     *
     * @return the priority value
     */
    int value();
}
