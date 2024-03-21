package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.StringUtils;

/**
 * Decode a Long.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class LongDecoder extends LeafDecoder<Long> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Long";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Long.class.isAssignableFrom(type.getRawType()) || long.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<Long> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        GResultOf<Long> results;

        String value = node.getValue().orElse("");
        if (StringUtils.isInteger(value)) {
            try {
                Long longVal = Long.parseLong(value);
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
