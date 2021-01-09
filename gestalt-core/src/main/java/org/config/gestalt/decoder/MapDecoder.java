package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.node.MapNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.Pair;
import org.config.gestalt.utils.ValidateOf;

import java.util.*;

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
                results = ValidateOf.inValid(new ValidationError.DecodingExpectedMap(path, genericInterfaces));
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

                        String nextPath = path != null && !path.isEmpty() ? path + "." + key : key;
                        ValidateOf<Object> keyValidate = decoderService.decodeNode(nextPath, new LeafNode(key),
                            (TypeCapture<Object>) keyType);
                        ValidateOf<Object> valueValidate = decoderService.decodeNode(nextPath, it.getValue(),
                            (TypeCapture<Object>) valueType);

                        errors.addAll(keyValidate.getErrors());
                        errors.addAll(valueValidate.getErrors());

                        if (!keyValidate.hasResults()) {
                            errors.add(new ValidationError.DecodersMapKeyNull(nextPath));
                        }
                        if (!valueValidate.hasResults()) {
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
