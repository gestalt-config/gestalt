package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ArrayNode;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ClassUtils;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.PathUtil;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.*;
import java.util.stream.Stream;

/**
 * Decode a Map. Assumes that the key is a simple class that can be decoded from a single string. ie a Boolean, String, Int.
 * The value can be any type we can decode.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class MapDecoder implements Decoder<Map<?, ?>> {

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
        return Map.class.isAssignableFrom(type.getRawType()) && type.hasParameter();
    }

    @Override
    public ValidateOf<Map<?, ?>> decode(String path, ConfigNode node, TypeCapture<?> type, DecoderService decoderService) {
        ValidateOf<Map<?, ?>> results;
        if (node instanceof MapNode) {
            MapNode mapNode = (MapNode) node;
            List<TypeCapture<?>> genericInterfaces = type.getParameterTypes();

            if (genericInterfaces == null || genericInterfaces.size() != 2) {
                results = ValidateOf.inValid(new ValidationError.DecodingExpectedMapNodeType(path, genericInterfaces, node));
            } else {
                TypeCapture<?> keyType = genericInterfaces.get(0);
                TypeCapture<?> valueType = genericInterfaces.get(1);

                List<ValidationError> errors = new ArrayList<>();

                var stream = mapNode.getMapNode().entrySet().stream();

                // if the value of the map is a primitive or a wrapper, flat map any entries that are map nodes.
                // if the value is a class, then we want to decode the map nodes into an object
                if (ClassUtils.isPrimitiveOrWrapper(valueType.getRawType())) {
                    stream = stream.flatMap(it -> convertMapToStream(it.getKey(), it));
                }

                Map<?, ?> map = stream.map(it -> {
                    String key = it.getKey();
                    if (key == null) {
                        errors.add(new ValidationError.DecodersMapKeyNull(path));
                        return null;
                    }

                    String nextPath = PathUtil.pathForKey(path, key);
                    ValidateOf<Object> keyValidate = decoderService.decodeNode(nextPath, new LeafNode(key), (TypeCapture<Object>) keyType);
                    ValidateOf<Object> valueValidate = decoderService.decodeNode(nextPath, it.getValue(), (TypeCapture<Object>) valueType);

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
            return ValidateOf.inValid(new ValidationError.DecodingExpectedMapNodeType(path, node));
        }
        return results;
    }

    private Stream<Map.Entry<String, ConfigNode>> convertMapToStream(String path, Map.Entry<String, ConfigNode> entry) {
        // if the key or entry is null, return the current entry and let later code deal with the null value.
        if (path == null || entry.getValue() == null) {
            return Stream.of(entry);
        } else if (entry.getValue() instanceof MapNode) {
            MapNode node = (MapNode) entry.getValue();

            return node.getMapNode().entrySet().stream().flatMap(it -> convertMapToStream(path + "." + it.getKey(), it));
        } else if (entry.getValue() instanceof ArrayNode) {
            ArrayNode node = (ArrayNode) entry.getValue();

            Stream<Map.Entry<String, ConfigNode>> stream = Stream.of();
            List<ConfigNode> nodes = node.getArray();

            for (int i = 0; i < nodes.size(); i++) {
                stream = Stream.concat(stream, convertMapToStream(path + "[" + i + "]", Map.entry("[" + i + "]", nodes.get(i))));
            }

            return stream;
        } else if (entry.getValue() instanceof LeafNode) {
            return Stream.of(Map.entry(path, entry.getValue()));
        } else {
            return Stream.of();
        }
    }
}
