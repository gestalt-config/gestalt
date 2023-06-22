package org.github.gestalt.config.guice;

import com.google.inject.MembersInjector;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.exceptions.GestaltException;

import java.lang.reflect.Field;

/**
 * Members Injector for Gestalt.
 *
 * @param <T> type of the field.
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class GestaltConfigMemberInjector<T> implements MembersInjector<T> {
    private final Field field;
    private final Gestalt gestalt;

    GestaltConfigMemberInjector(Field field, Gestalt gestalt) {
        this.field = field;
        field.setAccessible(true);

        this.gestalt = gestalt;
    }

    @SuppressWarnings("DoNotCall")
    @Override
    public void injectMembers(T t) {
        String path = "";
        try {
            InjectConfig[] annotation = field.getAnnotationsByType(InjectConfig.class);

            path = annotation[0].path();
            var config = gestalt.getConfig(path, field.getType());
            field.set(t, config);
        } catch (IllegalAccessException | GestaltException e) {
            throw new RuntimeException("Exception while injecting config type : " + field.getClass() + ", at path: " + path, e);
        }
    }
}
