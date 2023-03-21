package org.github.gestalt.config.decoder;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.OptionalDouble;

/**
 * Decodes an OptionalLong type
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class OptionalDoubleDecoder implements Decoder<OptionalDouble> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "OptionalDouble";
    }

    @Override
    public boolean matches(TypeCapture<?> type) {
        return OptionalDouble.class.isAssignableFrom(type.getRawType());
    }

    @Override
    public ValidateOf<OptionalDouble> decode(String path, ConfigNode node, TypeCapture<?> type, DecoderService decoderService) {
        // decode the generic type of the optional. Then we will wrap the result into an Optional
        ValidateOf<Double> optionalValue = decoderService.decodeNode(path, node, TypeCapture.of(Double.class));

        if(optionalValue.hasResults()) {
            return ValidateOf.validateOf(OptionalDouble.of(optionalValue.results()), optionalValue.getErrors());
        } else {
            return ValidateOf.validateOf(OptionalDouble.empty(), optionalValue.getErrors());
        }
    }
}
