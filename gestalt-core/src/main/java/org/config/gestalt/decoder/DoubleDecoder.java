package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.StringUtils;
import org.config.gestalt.utils.ValidateOf;

/**
 * Decode a double
 *
 * @author Colin Redmond
 */
public class DoubleDecoder extends LeafDecoder<Double> {

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
        return klass.isAssignableFrom(Double.class) || klass.isAssignableFrom(double.class);
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
