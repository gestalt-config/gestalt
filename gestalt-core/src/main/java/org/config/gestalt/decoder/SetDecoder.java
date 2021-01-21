package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Decode a Set type
 *
 * @author Colin Redmond
 */
public class SetDecoder extends CollectionDecoder<Set<?>> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Set";
    }

    @Override
    public boolean matches(TypeCapture<?> type) {
        return type.isAssignableFrom(Set.class) && type.hasParameter();
    }

    @Override
    protected ValidateOf<Set<?>> arrayDecode(String path, ConfigNode node, TypeCapture<?> klass, DecoderService decoderService) {
        List<ValidationError> errors = new ArrayList<>();
        Set<Object> results = new HashSet<>(node.size());

        for (int i = 0; i < node.size(); i++) {
            if (node.getIndex(i).isPresent()) {
                ConfigNode currentNode = node.getIndex(i).get();
                String nextPath = path != null && !path.isEmpty() ? path + "[" + i + "]" : "[" + i + "]";
                ValidateOf<?> validateOf = decoderService.decodeNode(nextPath, currentNode, klass.getFirstParameterType());

                errors.addAll(validateOf.getErrors());
                if (validateOf.hasResults()) {
                    results.add(validateOf.results());
                }

            } else {
                errors.add(new ValidationError.ArrayMissingIndex(i, path));
            }
        }


        return ValidateOf.validateOf(!results.isEmpty() ? results : null, errors);
    }
}
