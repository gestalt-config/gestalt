package org.github.gestalt.config.cdi;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Qualifier;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface GestaltConfigs {
    @Nonbinding
    String prefix() default "";

    final class Literal extends AnnotationLiteral<GestaltConfigs> implements GestaltConfigs {

        private final String prefix;

        public static Literal of(String prefix) {  //NOPMD
            return new Literal(prefix);
        }

        private Literal(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String prefix() {
            return this.prefix;
        }
    }
}
