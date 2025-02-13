package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ClassUtils;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.Pair;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.System.Logger.Level.TRACE;
import static org.github.gestalt.config.utils.PathUtil.*;

/**
 * Decode a Map. Assumes that the key is a simple class that can be decoded from a single string. ie a Boolean, String, Int.
 * The value can be any type we can decode.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class MapDecoder implements Decoder<Map<?, ?>> {

    private static final System.Logger logger = System.getLogger(MapDecoder.class.getName());

    Class<?> sequencedMap;

    Map<Class<?>, Supplier<Map>> supplierMap = new HashMap<>();

    public MapDecoder() {
        supplierMap.put(Map.class, HashMap::new);
        supplierMap.put(HashMap.class, HashMap::new);
        supplierMap.put(TreeMap.class, TreeMap::new);
        supplierMap.put(LinkedHashMap.class, LinkedHashMap::new);

        try {
            sequencedMap = Class.forName("java.util.SequencedMap");
            supplierMap.put(sequencedMap, LinkedHashMap::new);
        } catch (ClassNotFoundException e) {
            sequencedMap = null;
            logger.log(TRACE, "Unable to find class java.util.SequencedMap, SequencedMapDecoder disabled");
        }
    }

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Map";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Map.class.isAssignableFrom(type.getRawType()) && type.hasParameter() &&
            (node.getNodeType() == NodeType.MAP || node.getNodeType() == NodeType.LEAF);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public GResultOf<Map<?, ?>> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {
        GResultOf<Map<?, ?>> results;
        if (node instanceof LeafNode) {
            // if this is a leaf node, try and convert a single string in the format k1=v1,k2=v2 into a map node
            // once it has been converted to a map node recursively call this method to decode the new map node
            var optionalValue = node.getValue();
            if (optionalValue.isPresent()) {
                List<ValidationError> errors = new ArrayList<>();
                Map<String, ConfigNode> mapResult = new HashMap<>();

                // convert the string in the format k1=v1,k2=v2 to a map
                String value = optionalValue.get();
                String[] mapKeyValue = value.split("(?<!\\\\),");
                for (String entry : mapKeyValue) {
                    if (entry.isBlank()) {
                        continue;
                    }

                    String[] keyValuePair = entry.split("(?<!\\\\)=", 2);
                    if (keyValuePair.length != 2) {
                        errors.add(new ValidationError.MapEntryInvalid(path, entry, node, decoderContext));
                        continue;
                    }


                    var mapKey = keyValuePair[0].trim().replace("\\,", ",").replace("\\=", "=");
                    var mapValue = keyValuePair[1].trim().replace("\\,", ",").replace("\\=", "=");
                    mapResult.put(mapKey, new LeafNode(mapValue));
                }

                // if there are no errors try and decode the new map.
                // otherwise return the errors.
                if (errors.isEmpty()) {
                    results = decode(path, tags, new MapNode(mapResult), type, decoderContext);
                } else {
                    results = GResultOf.errors(errors);
                }
            } else {
                results = GResultOf.errors(new ValidationError.DecodingLeafMissingValue(path, name()));
            }

        } else if (node instanceof MapNode) {
            MapNode mapNode = (MapNode) node;
            List<TypeCapture<?>> genericInterfaces = type.getParameterTypes();

            if (genericInterfaces == null || genericInterfaces.size() != 2) {
                results = GResultOf.errors(new ValidationError.DecodingExpectedMapNodeType(path, genericInterfaces, node));
            } else {
                TypeCapture<?> keyType = genericInterfaces.get(0);
                TypeCapture<?> valueType = genericInterfaces.get(1);

                Supplier<Map> mapSupplier = supplierMap.get(type.getRawType());
                if (mapSupplier == null) {
                    logger.log(TRACE, "Unable to find supplier for " + type.getRawType() + ", defaulting to HashMap");
                    mapSupplier = supplierMap.get(Map.class);
                }

                List<ValidationError> errors = new ArrayList<>();

                var stream = mapNode.getMapNode().entrySet().stream();

                // if the value of the map is a primitive or a wrapper, flat map any entries that are map nodes.
                // if the value is a class, then we want to decode the map nodes into an object
                if (ClassUtils.isPrimitiveOrWrapper(valueType.getRawType())) {
                    stream = stream.flatMap(it -> convertMapToStream(it.getKey(), it, decoderContext));
                }

                Map<?, ?> map = stream.map(it -> {
                    String key = it.getKey();
                    if (key == null) {
                        errors.add(new ValidationError.DecodersMapKeyNull(path));
                        return null;
                    }

                    String nextPath = pathForKey(decoderContext.getDefaultLexer(), path, key);
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
                    .collect(mapSupplier, (m, v) -> m.put(v.getFirst(), v.getSecond()), Map::putAll);


                return GResultOf.resultOf(map, errors);
            }
        } else {
            return GResultOf.errors(new ValidationError.DecodingExpectedMapNodeType(path, node));
        }
        return results;
    }

    private Stream<Map.Entry<String, ConfigNode>> convertMapToStream(String path, Map.Entry<String, ConfigNode> entry,
                                                                     DecoderContext decoderContext) {

        SentenceLexer lexer = decoderContext.getDefaultLexer();
        // if the key or entry is null, return the current entry and let later code deal with the null value.
        if (path == null || entry.getValue() == null) {
            return Stream.of(entry);
        } else if (entry.getValue() instanceof MapNode) {
            MapNode node = (MapNode) entry.getValue();

            return node.getMapNode().entrySet()
                .stream()
                .flatMap(it -> convertMapToStream(pathForKey(lexer, path, it.getKey()), it, decoderContext));

        } else if (entry.getValue() instanceof ArrayNode) {
            ArrayNode node = (ArrayNode) entry.getValue();

            Stream<Map.Entry<String, ConfigNode>> stream = Stream.of();
            List<ConfigNode> nodes = node.getArray();

            for (int i = 0; i < nodes.size(); i++) {
                stream = Stream
                    .concat(stream, convertMapToStream(
                        pathForIndex(lexer, path, i),
                        Map.entry(forIndex(lexer, i), nodes.get(i)), decoderContext));
            }

            return stream;
        } else if (entry.getValue() instanceof LeafNode) {
            return Stream.of(Map.entry(path, entry.getValue()));
        } else {
            return Stream.of();
        }
    }
}
