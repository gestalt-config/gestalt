package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.PathUtil;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.ArrayList;
import java.util.List;

/**
 * Decode a list type.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class ListDecoder extends CollectionDecoder<List<?>> {

    @Override
    public String name() {
        return "List";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return List.class.isAssignableFrom(klass.getRawType()) && klass.hasParameter();
    }

    @Override
    protected ValidateOf<List<?>> arrayDecode(String path, ConfigNode node, TypeCapture<?> klass, DecoderContext decoderContext) {
        List<ValidationError> errors = new ArrayList<>();
        List<Object> results = new ArrayList<>(node.size());

        for (int i = 0; i < node.size(); i++) {
            if (node.getIndex(i).isPresent()) {
                ConfigNode currentNode = node.getIndex(i).get();
                String nextPath = PathUtil.pathForIndex(path, i);
                ValidateOf<?> validateOf = decoderContext.getDecoderService()
                    .decodeNode(nextPath, currentNode, klass.getFirstParameterType(), decoderContext);

                errors.addAll(validateOf.getErrors());
                if (validateOf.hasResults()) {
                    results.add(validateOf.results());
                }

            } else {
                errors.add(new ValidationError.ArrayMissingIndex(i));
                results.add(null);
            }
        }


        return ValidateOf.validateOf(!results.isEmpty() ? results : null, errors);
    }
}
