package org.github.gestalt.config.decoder;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;

/**
 * Decode a boolean.
 *
 * @author Colin Redmond
 */
public class BooleanDecoder extends LeafDecoder<Boolean> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Boolean";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return Boolean.class.isAssignableFrom(klass.getRawType()) || boolean.class.isAssignableFrom(klass.getRawType());
    }

    @Override
    protected ValidateOf<Boolean> leafDecode(String path, ConfigNode node) {
        String value = node.getValue().orElse("");
        return ValidateOf.valid(Boolean.parseBoolean(value));
    }
}
