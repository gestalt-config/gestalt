package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.StringUtils;

/**
 * Decode a Float.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class FloatDecoder extends LeafDecoder<Float> {

    private boolean treatEmptyStringsAsNull = false;

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Float";
    }

    @Override
    public void applyConfig(GestaltConfig config) {
        this.treatEmptyStringsAsNull = config.isTreatEmptyStringsAsNull();
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Float.class.isAssignableFrom(type.getRawType()) || float.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<Float> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        String value = node.getValue().orElse("");

        // Check if empty string should be treated as null
        if (value.isEmpty() && treatEmptyStringsAsNull) {
            return GResultOf.result(null);
        }

        if (StringUtils.isReal(value)) {
            try {
                Float floatVal = Float.parseFloat(value);
                return GResultOf.result(floatVal);
            } catch (NumberFormatException e) {
                return GResultOf.errors(
                    new ValidationError.DecodingNumberFormatException(path, node, name(), decoderContext));
            }
        } else {
            return GResultOf.errors(new ValidationError.DecodingNumberParsing(path, node, name()));
        }
    }
}
