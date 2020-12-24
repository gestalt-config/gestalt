package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ArrayDecoder extends CollectionDecoder {

    @Override
    public String name() {
        return "Array";
    }

    @Override
    public boolean matches(TypeCapture<?> type) {
        return type.getRawType().isArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> ValidateOf<T> arrayDecode(String path, ConfigNode node, TypeCapture<T> klass, DecoderService decoderService) {
        List<ValidationError> errors = new ArrayList<>();
        Object[] results = (Object[]) Array.newInstance(klass.getComponentType(), node.size());

        for (int i = 0; i < node.size(); i++) {
            if (node.getIndex(i).isPresent()) {
                ConfigNode currentNode = node.getIndex(i).get();
                String nextPath = path != null && !path.isEmpty() ? path + "[" + i + "]" : "[" + i + "]";
                ValidateOf<?> validateOf = decoderService.decodeNode(nextPath, currentNode, TypeCapture.of(klass.getComponentType()));

                errors.addAll(validateOf.getErrors());
                if (validateOf.hasResults()) {
                    results[i] = validateOf.results();
                }

            } else {
                errors.add(new ValidationError.ArrayMissingIndex(i));
                results[i] = null;
            }
        }

        return ValidateOf.validateOf((T) results, errors);
    }
}
