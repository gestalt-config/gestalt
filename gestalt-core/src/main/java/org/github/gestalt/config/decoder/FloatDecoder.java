package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.StringUtils;

/**
 * Decode a Float.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class FloatDecoder extends LeafDecoder<Float> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Float";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Float.class.isAssignableFrom(type.getRawType()) || float.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<Float> leafDecode(String path, ConfigNode node) {
        GResultOf<Float> results;

        String value = node.getValue().orElse("");
        if (StringUtils.isReal(value)) {
            try {
                Float floatVal = Float.parseFloat(value);
                results = GResultOf.result(floatVal);
            } catch (NumberFormatException e) {
                results = GResultOf.errors(new ValidationError.DecodingNumberFormatException(path, node, name()));
            }
        } else {
            results = GResultOf.errors(new ValidationError.DecodingNumberParsing(path, node, name()));
        }

        return results;
    }
}
