package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.StringUtils;
import org.github.gestalt.config.utils.ValidateOf;

/**
 * Decode a double.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class DoubleDecoder extends LeafDecoder<Double> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Double";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return Double.class.isAssignableFrom(klass.getRawType()) || double.class.isAssignableFrom(klass.getRawType());
    }

    @Override
    protected ValidateOf<Double> leafDecode(String path, ConfigNode node) {
        ValidateOf<Double> results;

        String value = node.getValue().orElse("");
        if (StringUtils.isReal(value)) {
            try {
                Double longVal = Double.parseDouble(value);
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
