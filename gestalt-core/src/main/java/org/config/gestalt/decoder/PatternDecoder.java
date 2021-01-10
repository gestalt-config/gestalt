package org.config.gestalt.decoder;

import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

import java.util.regex.Pattern;

public class PatternDecoder extends LeafDecoder<Pattern> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Pattern";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.isAssignableFrom(Pattern.class);
    }

    @Override
    protected ValidateOf<Pattern> leafDecode(String path, ConfigNode node) {
        Pattern pattern = Pattern.compile(node.getValue().orElse(""), Pattern.CASE_INSENSITIVE);
        return ValidateOf.valid(pattern);
    }
}
