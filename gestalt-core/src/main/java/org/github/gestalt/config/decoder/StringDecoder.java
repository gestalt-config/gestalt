package org.github.gestalt.config.decoder;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;

/**
 * Decode a String.
 *
 * @author Colin Redmond
 */
public class StringDecoder extends LeafDecoder<String> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "String";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.isAssignableFrom(String.class);
    }

    @Override
    protected ValidateOf<String> leafDecode(String path, ConfigNode node) {
        return ValidateOf.valid(node.getValue().orElse(""));
    }
}
