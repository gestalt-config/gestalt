package org.github.gestalt.config.decoder;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.Optional;

/**
 * Decodes a generic optional type.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class OptionalDecoder implements Decoder<Optional<?>> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Optional";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Optional.class.isAssignableFrom(type.getRawType());
    }

    @Override
    public ValidateOf<Optional<?>> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {
        // decode the generic type of the optional. Then we will wrap the result into an Optional
        ValidateOf<?> optionalValue = decoderContext.getDecoderService()
            .decodeNode(path, tags, node, type.getFirstParameterType(), decoderContext);

        return ValidateOf.validateOf(Optional.ofNullable(optionalValue.results()), optionalValue.getErrors());
    }
}
