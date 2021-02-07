package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.StringUtils;
import org.github.gestalt.config.utils.ValidateOf;

/**
 * Decode a Float.
 *
 * @author Colin Redmond
 */
public class FloatDecoder extends LeafDecoder<Float> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Float";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.isAssignableFrom(Float.class) || klass.isAssignableFrom(float.class);
    }

    @Override
    protected ValidateOf<Float> leafDecode(String path, ConfigNode node) {
        ValidateOf<Float> results;

        String value = node.getValue().orElse("");
        if (StringUtils.isReal(value)) {
            try {
                Float floatVal = Float.parseFloat(value);
                results = ValidateOf.valid(floatVal);
            } catch (NumberFormatException e) {
                results = ValidateOf.inValid(new ValidationError.DecodingNumberFormatException(path, node, name()));
            }
        } else {
            results = ValidateOf.inValid(new ValidationError.DecodingNumberParsing(path, node, name()));
        }

        return results;
    }
}
