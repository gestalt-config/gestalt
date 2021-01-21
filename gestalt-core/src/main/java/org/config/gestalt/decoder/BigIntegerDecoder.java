package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.StringUtils;
import org.config.gestalt.utils.ValidateOf;

import java.math.BigInteger;

/**
 * Decode Big Integers
 * @author Colin Redmond
 */
public class BigIntegerDecoder extends LeafDecoder<BigInteger> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "BigInteger";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return klass.isAssignableFrom(BigInteger.class);
    }

    @Override
    protected ValidateOf<BigInteger> leafDecode(String path, ConfigNode node) {
        ValidateOf<BigInteger> results;

        String value = node.getValue().orElse("");
        if (StringUtils.isReal(value)) {
            try {
                long longVal = Long.parseLong(value);
                BigInteger bigDecimal = BigInteger.valueOf(longVal);
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
