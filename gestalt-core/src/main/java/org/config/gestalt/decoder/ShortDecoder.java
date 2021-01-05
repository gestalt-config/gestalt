package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.StringUtils;
import org.config.gestalt.utils.ValidateOf;

public class ShortDecoder extends LeafDecoder<Short> {

    @Override
    public String name() {
        return "Short";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.isAssignableFrom(Short.class) || klass.isAssignableFrom(short.class);
    }

    @Override
    protected ValidateOf<Short> leafDecode(String path, ConfigNode node) {
        ValidateOf<Short> results;

        String value = node.getValue().orElse("");
        if (StringUtils.isInteger(value)) {
            try {
                Short intVal = Short.parseShort(value);
                results = ValidateOf.valid(intVal);
            } catch (NumberFormatException e) {
                results = ValidateOf.inValid(new ValidationError.DecodingNumberFormatException(path, node, name()));
            }
        } else {
            results = ValidateOf.inValid(new ValidationError.DecodingNumberParsing(path, node, name()));
        }

        return results;
    }
}
