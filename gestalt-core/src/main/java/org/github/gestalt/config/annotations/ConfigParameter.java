package org.github.gestalt.config.annotations;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotate a Parameters or a method to optional modify the path or optionally add a default.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
@Target(value = {PARAMETER})
@Inherited
@Retention(value = RUNTIME)
public @interface ConfigParameter {
    String path() default "";

    String defaultVal() default "";
}
