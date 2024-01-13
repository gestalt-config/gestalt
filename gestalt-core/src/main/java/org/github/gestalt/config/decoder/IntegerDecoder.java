package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.StringUtils;

/**
 * Decode an Integer.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class IntegerDecoder extends LeafDecoder<Integer> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Integer";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Integer.class.isAssignableFrom(type.getRawType()) || int.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<Integer> leafDecode(String path, ConfigNode node) {
        GResultOf<Integer> results;

        String value = node.getValue().orElse("");
        if (StringUtils.isInteger(value)) {
            try {
                Integer intVal = Integer.parseInt(value);
                results = GResultOf.result(intVal);
            } catch (NumberFormatException e) {
                results = GResultOf.errors(new ValidationError.DecodingNumberFormatException(path, node, name()));
            }
        } else {
            results = GResultOf.errors(new ValidationError.DecodingNumberParsing(path, node, name()));
        }

        return results;
    }
}
