package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.StringUtils;
import org.config.gestalt.utils.ValidateOf;

import java.math.BigDecimal;

public class BigDecimalDecoder extends LeafDecoder<BigDecimal> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "BigDecimal";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.isAssignableFrom(BigDecimal.class);
    }

    @Override
    protected ValidateOf<BigDecimal> leafDecode(String path, ConfigNode node) {
        ValidateOf<BigDecimal> results;

        String value = node.getValue().orElse("");
        if (StringUtils.isReal(value)) {
            try {
                double doubleValue = Double.parseDouble(value);
                BigDecimal bigDecimal = BigDecimal.valueOf(doubleValue);
                results = ValidateOf.valid(bigDecimal);
            } catch (NumberFormatException e) {
                results = ValidateOf.inValid(new ValidationError.DecodingNumberFormatException(path, node, name()));
            }
        } else {
            results = ValidateOf.inValid(new ValidationError.DecodingNumberParsing(path, node, name()));
        }

        return results;
    }
}
