package org.github.gestalt.config.guice;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.github.gestalt.config.Gestalt;

import java.lang.reflect.Field;

/**
 * Configures a type listener for the annotation @InjectConfig.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
class GestaltConfigTypeListener implements TypeListener {

    private final Gestalt gestalt;

    GestaltConfigTypeListener(Gestalt gestalt) {
        this.gestalt = gestalt;
    }

    @Override
    public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
        Class<?> clazz = typeLiteral.getRawType();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(InjectConfig.class)) {
                    typeEncounter.register(new GestaltConfigMemberInjector<>(field, gestalt));
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}
