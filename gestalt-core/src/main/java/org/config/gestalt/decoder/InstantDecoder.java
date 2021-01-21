package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Decode an Instant
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
