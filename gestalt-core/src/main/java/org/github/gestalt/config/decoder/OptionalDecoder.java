package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

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
    public GResultOf<Optional<?>> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {
        if (node != null) {
            // decode the generic type of the optional. Then we will wrap the result into an Optional
            GResultOf<?> optionalValue = decoderContext.getDecoderService()
                .decodeNode(path, tags, node, type.getFirstParameterType(), decoderContext);

            if (optionalValue.hasResults()) {
                return GResultOf.resultOf(Optional.of(optionalValue.results()), optionalValue.getErrors());
            } else {
                var errors = optionalValue.getErrorsNotLevel(ValidationLevel.MISSING_VALUE);
                errors.add(new ValidationError.OptionalMissingValueDecoding(path, node, name()));
                return GResultOf.resultOf(Optional.ofNullable(optionalValue.results()), errors);
            }
        } else {
            return GResultOf.resultOf(Optional.empty(), new ValidationError.OptionalMissingValueDecoding(path, name()));
        }
    }
}
