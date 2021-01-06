package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.StringUtils;
import org.config.gestalt.utils.ValidateOf;

public class LongDecoder extends LeafDecoder<Long> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Long";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.isAssignableFrom(Long.class) || klass.isAssignableFrom(long.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ValidateOf<Long> leafDecode(String path, ConfigNode node) {
        ValidateOf<Long> results;

        String value = node.getValue().orElse("");
        if (StringUtils.isInteger(value)) {
            try {
                Long longVal = Long.parseLong(value);
                results = ValidateOf.valid(longVal);
            } catch (NumberFormatException e) {
                results = ValidateOf.inValid(new ValidationError.DecodingNumberFormatException(path, node, name()));
            }
        } else {
            results = ValidateOf.inValid(new ValidationError.DecodingNumberParsing(path, node, name()));
        }

        return results;
    }
}
