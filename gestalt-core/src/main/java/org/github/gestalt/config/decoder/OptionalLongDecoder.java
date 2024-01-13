package org.github.gestalt.config.decoder;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.OptionalLong;

/**
 * Decodes an OptionalLong type.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class OptionalLongDecoder implements Decoder<OptionalLong> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "OptionalLong";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return OptionalLong.class.isAssignableFrom(type.getRawType());
    }

    @Override
    public GResultOf<OptionalLong> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {
        // decode the generic type of the optional. Then we will wrap the result into an Optional
        GResultOf<Long> optionalValue = decoderContext.getDecoderService()
            .decodeNode(path, tags, node, TypeCapture.of(Long.class), decoderContext);

        if (optionalValue.hasResults()) {
            return GResultOf.resultOf(OptionalLong.of(optionalValue.results()), optionalValue.getErrors());
        } else {
            return GResultOf.resultOf(OptionalLong.empty(), optionalValue.getErrors());
        }
    }
}
