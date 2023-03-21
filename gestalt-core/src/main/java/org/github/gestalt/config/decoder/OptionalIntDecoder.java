package org.github.gestalt.config.decoder;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.OptionalInt;

/**
 * Decodes an OptionalInt type.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class OptionalIntDecoder implements Decoder<OptionalInt> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "OptionalInt";
    }

    @Override
    public boolean matches(TypeCapture<?> type) {
        return OptionalInt.class.isAssignableFrom(type.getRawType());
    }

    @Override
    public ValidateOf<OptionalInt> decode(String path, ConfigNode node, TypeCapture<?> type, DecoderService decoderService) {
        // decode the generic type of the optional. Then we will wrap the result into an Optional
        ValidateOf<Integer> optionalValue = decoderService.decodeNode(path, node, TypeCapture.of(Integer.class));

        if (optionalValue.hasResults()) {
            return ValidateOf.validateOf(OptionalInt.of(optionalValue.results()), optionalValue.getErrors());
        } else {
            return ValidateOf.validateOf(OptionalInt.empty(), optionalValue.getErrors());
        }
    }
}
