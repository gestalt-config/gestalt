package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.*;

/**
 * Decode a Map. Assumes that the key is a simple class that can be decoded from a single string. ie a Boolean, String, Int.
 * The value can be any type we can decode.
 *
 * @author Colin Redmond
 */
public class MapDecoder implements Decoder<Map<?, ?>> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Map";
    }

    @Override
    public boolean matches(TypeCapture<?> type) {
        return type.isAssignableFrom(Map.class) && type.hasParameter();
    }

    @Override
    public ValidateOf<Map<?, ?>> decode(String path, ConfigNode node, TypeCapture<?> type, DecoderService decoderService) {
        ValidateOf<Map<?, ?>> results;
        if (node instanceof MapNode) {
            MapNode mapNode = (MapNode) node;
            List<TypeCapture<?>> genericInterfaces = type.getParameterTypes();

            if (genericInterfaces == null || genericInterfaces.size() != 2) {
                results = ValidateOf.inValid(new ValidationError.DecodingExpectedMapNodeType(path, genericInterfaces));
            } else {
                TypeCapture<?> keyType = genericInterfaces.get(0);
                TypeCapture<?> valueType = genericInterfaces.get(1);

                List<ValidationError> errors = new ArrayList<>();

                Map<?, ?> map = mapNode.getMapNode().entrySet().stream()
                    .map(it -> {
                        String key = it.getKey();
                        if (key == null) {
                            errors.add(new ValidationError.DecodersMapKeyNull(path));
                            return null;
                        }

                        String nextPath = path != null && ! path.isEmpty() ? path + "." + key : key;
                        ValidateOf<Object> keyValidate = decoderService.decodeNode(nextPath, new LeafNode(key),
                            (TypeCapture<Object>) keyType);
                        ValidateOf<Object> valueValidate = decoderService.decodeNode(nextPath, it.getValue(),
                            (TypeCapture<Object>) valueType);

                        errors.addAll(keyValidate.getErrors());
                        errors.addAll(valueValidate.getErrors());

                        if (! keyValidate.hasResults()) {
                            errors.add(new ValidationError.DecodersMapKeyNull(nextPath));
                        }
                        if (! valueValidate.hasResults()) {
                            errors.add(new ValidationError.DecodersMapValueNull(nextPath));
                        }

                        if (keyValidate.hasResults()) {
                            return new Pair<>(keyValidate.results(), valueValidate.results());
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(HashMap::new, (m, v) -> m.put(v.getFirst(), v.getSecond()), HashMap::putAll);


                return ValidateOf.validateOf(map, errors);
            }
        } else {
            results = ValidateOf.inValid(new ValidationError.DecodingExpectedLeafNodeType(path, node, name()));
        }
        return results;
    }

}
