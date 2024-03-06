package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.OptionalInt;

/**
 * Decodes an OptionalInt type.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class OptionalIntDecoder implements Decoder<OptionalInt> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "OptionalInt";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return OptionalInt.class.isAssignableFrom(type.getRawType());
    }

    @Override
    public GResultOf<OptionalInt> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {
        if (node instanceof LeafNode && node.getValue().isPresent()) {
            // decode the generic type of the optional. Then we will wrap the result into an Optional
            GResultOf<Integer> optionalValue = decoderContext.getDecoderService()
                .decodeNode(path, tags, node, TypeCapture.of(Integer.class), decoderContext);

            if (optionalValue.hasResults()) {
                return GResultOf.resultOf(OptionalInt.of(optionalValue.results()), optionalValue.getErrors());
            } else {
                return GResultOf.resultOf(OptionalInt.empty(), optionalValue.getErrors());
            }
        } else {
            return GResultOf.resultOf(OptionalInt.empty(), new ValidationError.OptionalMissingValueDecoding(path, node, name()));
        }
    }
}
