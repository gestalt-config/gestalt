package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

import java.util.ArrayList;
import java.util.List;

public class ListDecoder extends CollectionDecoder<List<?>> {

    @Override
    public String name() {
        return "List";
    }

    @Override
    public boolean matches(TypeCapture<?> type) {
        return type.isAssignableFrom(List.class) && type.hasParameter();
    }

    @Override
    protected ValidateOf<List<?>> arrayDecode(String path, ConfigNode node, TypeCapture<?> klass, DecoderService decoderService) {
        List<ValidationError> errors = new ArrayList<>();
        List<Object> results = new ArrayList<>(node.size());

        for (int i = 0; i < node.size(); i++) {
            if (node.getIndex(i).isPresent()) {
                ConfigNode currentNode = node.getIndex(i).get();
                String nextPath = path != null && !path.isEmpty() ? path + "[" + i + "]" : "[" + i + "]";
                ValidateOf<?> validateOf = decoderService.decodeNode(nextPath, currentNode, TypeCapture.of(klass.getParameterType()));

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
