package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.StringUtils;

import java.math.BigDecimal;

/**
 * Decode Big Decimals.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class BigDecimalDecoder extends LeafDecoder<BigDecimal> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "BigDecimal";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return BigDecimal.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<BigDecimal> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        GResultOf<BigDecimal> results;

        String value = node.getValue().orElse("");
        if (StringUtils.isReal(value)) {
            try {
                double doubleValue = Double.parseDouble(value);
                BigDecimal bigDecimal = BigDecimal.valueOf(doubleValue);
                results = GResultOf.result(bigDecimal);
            } catch (NumberFormatException e) {
                results = GResultOf.errors(
                    new ValidationError.DecodingNumberFormatException(path, node, name(), decoderContext));
            }
        } else {
            results = GResultOf.errors(new ValidationError.DecodingNumberParsing(path, node, name()));
        }

        return results;
    }
}
