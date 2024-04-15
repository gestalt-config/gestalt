package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.UUID;

/**
 * Decode a UUID.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class UUIDDecoder extends LeafDecoder<UUID> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "UUID";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return UUID.class.isAssignableFrom(type.getRawType());
    }

    @Override
    protected GResultOf<UUID> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
        String value = node.getValue().orElse("");

        try {
            return GResultOf.result(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            return GResultOf.errors(
                new ValidationError.ErrorDecodingException(path, node, name(), e.getMessage(), decoderContext));
        }
    }
}
