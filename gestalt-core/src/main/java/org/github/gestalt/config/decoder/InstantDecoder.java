package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Decode an Instant.
 *
 * @author Colin Redmond
 */
public class InstantDecoder extends LeafDecoder<Instant> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Instant";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.isAssignableFrom(Instant.class);
    }

    @Override
    protected ValidateOf<Instant> leafDecode(String path, ConfigNode node) {
        ValidateOf<Instant> results;

        String value = node.getValue().orElse("");
        try {
            Instant instant = Instant.parse(value);
            results = ValidateOf.valid(instant);
        } catch (DateTimeParseException e) {
            results = ValidateOf.inValid(new ValidationError.ErrorDecodingException(path, node, name()));
        }
        return results;
    }
}
