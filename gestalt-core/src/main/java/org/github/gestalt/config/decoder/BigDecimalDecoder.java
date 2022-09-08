package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.StringUtils;
import org.github.gestalt.config.utils.ValidateOf;

import java.math.BigDecimal;

/**
 * Decode Big Decimals.
 *
 * @author Colin Redmond
 */
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
        return BigDecimal.class.isAssignableFrom(klass.getRawType());
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
