package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.StringUtils;
import org.config.gestalt.utils.ValidateOf;

public class FloatDecoder extends LeafDecoder {

    @Override
    public String name() {
        return "Float";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.isAssignableFrom(Float.class) || klass.isAssignableFrom(float.class);
    }

    @Override
    @SuppressWarnings("unchecked")
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
