package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.StringUtils;
import org.config.gestalt.utils.ValidateOf;

import java.time.Duration;

/**
 * Decode a duration.
 *
 * @author Colin Redmond
 */
public class DurationDecoder extends LeafDecoder<Duration> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Duration";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.isAssignableFrom(Duration.class);
    }

    @Override
    protected ValidateOf<Duration> leafDecode(String path, ConfigNode node) {
        ValidateOf<Duration> results;

        String value = node.getValue().orElse("");
        if (StringUtils.isInteger(value)) {
            try {
                long longVal = Long.parseLong(value);
                results = ValidateOf.valid(Duration.ofMillis(longVal));
            } catch (NumberFormatException e) {
                results = ValidateOf.inValid(new ValidationError.ErrorDecodingException(path, node, name()));
            }
        } else {
            results = ValidateOf.inValid(new ValidationError.DecodingNumberParsing(path, node, name()));
        }
        return results;
    }
}
