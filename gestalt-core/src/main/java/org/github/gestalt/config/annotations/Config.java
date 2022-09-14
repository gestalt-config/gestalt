package org.github.gestalt.config.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value = {FIELD, METHOD})
@Inherited
@Retention(value = RUNTIME)
@Documented
public @interface Config {
    String path() default "";
    String defaultVal() default "";
}
