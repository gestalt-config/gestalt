package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.StringUtils;

import java.math.BigInteger;

/**
 * Decode Big Integers.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class BigIntegerDecoder extends LeafDecoder<BigInteger> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "BigInteger";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return BigInteger.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<BigInteger> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        GResultOf<BigInteger> results;

        String value = node.getValue().orElse("");
        if (StringUtils.isReal(value)) {
            try {
                long longVal = Long.parseLong(value);
                BigInteger bigDecimal = BigInteger.valueOf(longVal);
                results = GResultOf.result(bigDecimal);
            } catch (NumberFormatException e) {
                results = GResultOf.errors(
                    new ValidationError.DecodingNumberFormatException(path, node, name(), decoderContext.getSecretConcealer()));
            }
        } else {
            results = GResultOf.errors(new ValidationError.DecodingNumberParsing(path, node, name()));
        }

        return results;
    }
}
