package org.github.gestalt.config.decoder;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.OptionalDouble;

/**
 * Decodes an OptionalDouble type.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class OptionalDoubleDecoder implements Decoder<OptionalDouble> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "OptionalDouble";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return OptionalDouble.class.isAssignableFrom(type.getRawType());
    }

    @Override
    public GResultOf<OptionalDouble> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {
        // decode the generic type of the optional. Then we will wrap the result into an Optional
        GResultOf<Double> optionalValue = decoderContext.getDecoderService()
            .decodeNode(path, tags, node, TypeCapture.of(Double.class), decoderContext);

        if (optionalValue.hasResults()) {
            return GResultOf.resultOf(OptionalDouble.of(optionalValue.results()), optionalValue.getErrors());
        } else {
            return GResultOf.resultOf(OptionalDouble.empty(), optionalValue.getErrors());
        }
    }
}
