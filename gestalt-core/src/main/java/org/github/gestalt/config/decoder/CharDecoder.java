package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.ArrayList;
import java.util.List;

/**
 * Decode char.
 *
 * @author Colin Redmond
 */
public class CharDecoder extends LeafDecoder<Character> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Character";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.isAssignableFrom(Character.class) || klass.isAssignableFrom(char.class);
    }

    @Override
    protected ValidateOf<Character> leafDecode(String path, ConfigNode node) {
        Character results = null;
        List<ValidationError> error = new ArrayList<>();

        String value = node.getValue().orElse("");
        if (value.length() > 0) {
            results = value.charAt(0);
        }

        if (value.length() != 1) {
            error.add(new ValidationError.DecodingCharWrongSize(path, node));
        }


        return ValidateOf.validateOf(results, error);
    }
}
