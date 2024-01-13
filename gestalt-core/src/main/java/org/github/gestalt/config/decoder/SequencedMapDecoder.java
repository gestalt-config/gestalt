package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ArrayNode;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ClassUtils;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.PathUtil;

import java.util.*;
import java.util.stream.Stream;

/**
 * Decode a Map. Assumes that the key is a simple class that can be decoded from a single string. ie a Boolean, String, Int.
 * The value can be any type we can decode.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class SequencedMapDecoder implements Decoder<Map<?, ?>> {

    private static final System.Logger logger = System.getLogger(SequencedMapDecoder.class.getName());

    Class<?> sequencedMap;

    public SequencedMapDecoder() {
        try {
            sequencedMap = Class.forName("java.util.SequencedMap");
        } catch (ClassNotFoundException e) {
            sequencedMap = null;
            logger.log(System.Logger.Level.TRACE, "Unable to find class java.util.SequencedMap, SequencedMapDecoder disabled");
        }
    }

    @Override
    public Priority priority() {
        return Priority.HIGH;
    }

    @Override
    public String name() {
        return "SequencedMap";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return sequencedMap != null && sequencedMap.isAssignableFrom(type.getRawType()) && type.hasParameter();
    }

    @SuppressWarnings("unchecked")
    @Override
    public GResultOf<Map<?, ?>> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {
        GResultOf<Map<?, ?>> results;
        if (node instanceof MapNode) {
            MapNode mapNode = (MapNode) node;
            List<TypeCapture<?>> genericInterfaces = type.getParameterTypes();

            if (genericInterfaces == null || genericInterfaces.size() != 2) {
                results = GResultOf.errors(new ValidationError.DecodingExpectedMapNodeType(path, genericInterfaces, node));
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
                    GResultOf<Object> keyValidate = decoderContext.getDecoderService()
                        .decodeNode(nextPath, tags, new LeafNode(key), (TypeCapture<Object>) keyType, decoderContext);
                    GResultOf<Object> valueValidate = decoderContext.getDecoderService()
                        .decodeNode(nextPath, tags, it.getValue(), (TypeCapture<Object>) valueType, decoderContext);

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
                    .collect(LinkedHashMap::new, (m, v) -> m.put(v.getFirst(), v.getSecond()), LinkedHashMap::putAll);


                return GResultOf.resultOf(map, errors);
            }
        } else {
            return GResultOf.errors(new ValidationError.DecodingExpectedMapNodeType(path, node));
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
