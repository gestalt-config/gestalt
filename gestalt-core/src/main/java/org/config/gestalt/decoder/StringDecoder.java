package org.config.gestalt.decoder;

import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

public class StringDecoder extends LeafDecoder<String> {

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
