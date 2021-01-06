package org.config.gestalt.decoder;

import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

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
        return klass.isAssignableFrom(Boolean.class) || klass.isAssignableFrom(boolean.class);
    }

    @Override
    protected ValidateOf<Boolean> leafDecode(String path, ConfigNode node) {
        String value = node.getValue().orElse("");
        return ValidateOf.valid(Boolean.parseBoolean(value));
    }
}
