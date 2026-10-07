package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.StringUtils;

/**
 * Decode Byte.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class ByteDecoder extends LeafDecoder<Byte> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Byte";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Byte.class.isAssignableFrom(type.getRawType()) || byte.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<Byte> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        GResultOf<Byte> results;

        String value = node.getValue().orElse("");
        if (StringUtils.isInteger(value)) {
            try {
                Byte byteValue = Byte.parseByte(value);
                results = GResultOf.result(byteValue);
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
