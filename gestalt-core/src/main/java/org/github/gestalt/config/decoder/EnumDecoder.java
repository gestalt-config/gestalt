package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ValidateOf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Decode an Enum.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class EnumDecoder<T extends Enum<T>> extends LeafDecoder<T> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Enum";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return type.getRawType().isEnum();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ValidateOf<T> leafDecode(String path, ConfigNode node, TypeCapture<?> type) {
        String value = node.getValue().orElse("");
        try {
            Class<?> klass = type.getRawType();
            Method m = klass.getMethod("name");

            Object[] enumConstants = klass.getEnumConstants();
            for (Object enumConst : enumConstants) {
                Object enumName = m.invoke(enumConst);
                if (enumName instanceof String && value.equalsIgnoreCase((String) enumName)) {
                    return ValidateOf.valid((T) enumConst);
                }
            }

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return ValidateOf.inValid(new ValidationError.ExceptionDecodingEnum(path, value, type.getRawType(), e));
        }

        return ValidateOf.inValid(new ValidationError.EnumValueNotFound(path, value, type.getRawType()));
    }

    @Override
    protected ValidateOf<T> leafDecode(String path, ConfigNode node) {
        // not called.
        return null;
    }
}
