package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EnumDecoder extends LeafDecoder {

    @Override
    public String name() {
        return "Enum";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.getRawType().isEnum();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> ValidateOf<T> leafDecode(String path, ConfigNode node, TypeCapture<T> type) {
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
    protected <T> ValidateOf<T> leafDecode(String path, ConfigNode node) {
        // not called.
        return null;
    }
}
