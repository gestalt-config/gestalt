package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.StringUtils;

/**
 * Decode an Integer.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class IntegerDecoder extends LeafDecoder<Integer> {

    private boolean treatEmptyStringsAsNull = false;

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Integer";
    }

    @Override
    public void applyConfig(GestaltConfig config) {
        this.treatEmptyStringsAsNull = config.isTreatEmptyStringsAsNull();
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Integer.class.isAssignableFrom(type.getRawType()) || int.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<Integer> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        String value = node.getValue().orElse("");

        // Check if empty string should be treated as null
        if (value.isEmpty() && treatEmptyStringsAsNull) {
            return GResultOf.result(null);
        }

        if (StringUtils.isInteger(value)) {
            try {
                Integer intVal = Integer.parseInt(value);
                return GResultOf.result(intVal);
            } catch (NumberFormatException e) {
                return GResultOf.errors(
                    new ValidationError.DecodingNumberFormatException(path, node, name(), decoderContext));
            }
        } else {
            return GResultOf.errors(new ValidationError.DecodingNumberParsing(path, node, name()));
        }
    }
}
