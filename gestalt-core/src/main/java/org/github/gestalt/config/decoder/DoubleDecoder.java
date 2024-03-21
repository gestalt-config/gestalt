package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.StringUtils;

/**
 * Decode a double.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
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
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Double.class.isAssignableFrom(type.getRawType()) || double.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<Double> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        GResultOf<Double> results;

        String value = node.getValue().orElse("");
        if (StringUtils.isReal(value)) {
            try {
                Double longVal = Double.parseDouble(value);
                results = GResultOf.result(longVal);
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
