package org.github.gestalt.config.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value={TYPE,PARAMETER})
@Retention(value=RUNTIME)
@Documented
public @interface ConfigPriority {
    int value();
}
