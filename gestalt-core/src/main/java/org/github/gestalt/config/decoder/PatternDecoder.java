package org.github.gestalt.config.decoder;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.regex.Pattern;

/**
 * Decode a Pattern.
 *
 * @author Colin Redmond
 */
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
