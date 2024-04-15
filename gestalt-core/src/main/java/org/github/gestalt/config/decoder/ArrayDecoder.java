package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ArrayNode;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.PathUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Decodes primitive arrays of any type.
 *
 * @param <T> type of array
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class ArrayDecoder<T> implements Decoder<T[]> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Array";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return type.isArray();
    }

    @Override
    public GResultOf<T[]> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {
        GResultOf<T[]> results;
        if (node instanceof ArrayNode) {
            results = arrayDecode(path, tags, node, type, decoderContext);
        } else if (node instanceof LeafNode) {
            var valueOptional = node.getValue();
            if (valueOptional.isPresent()) {
                String value = valueOptional.get();
                String[] array = value.split("(?<!\\\\),");
                List<ConfigNode> leafNodes = Arrays.stream(array)
                    .map(String::trim)
                    .map(it -> it.replace("\\,", ","))
                    .map(LeafNode::new)
                    .collect(Collectors.toList());

                results = arrayDecode(path, tags, new ArrayNode(leafNodes), type, decoderContext);
            } else {
                results = GResultOf.errors(new ValidationError.DecodingLeafMissingValue(path, name()));
            }
        } else {
            results = GResultOf.errors(new ValidationError.DecodingExpectedArrayNodeType(path, node, name()));
        }
        return results;
    }

    /**
     * Decode an array values.
     *
     * @param path           Current path we are decoding
     * @param tags           tags for the current decoding
     * @param node           current node we are decoding
     * @param klass          class to decode into
     * @param decoderContext The decoder context
     * @return GResultOf array built from the config node
     */
    @SuppressWarnings("unchecked")
    private GResultOf<T[]> arrayDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> klass, DecoderContext decoderContext) {
        List<ValidationError> errors = new ArrayList<>();
        T[] results = (T[]) Array.newInstance(klass.getComponentType(), node.size());

        for (int i = 0; i < node.size(); i++) {
            var valueOptional = node.getIndex(i);
            if (valueOptional.isPresent()) {
                ConfigNode currentNode = valueOptional.get();
                String nextPath = PathUtil.pathForIndex(decoderContext.getDefaultLexer(), path, i);
                GResultOf<?> resultOf = decoderContext.getDecoderService()
                    .decodeNode(nextPath, tags, currentNode, TypeCapture.of(klass.getComponentType()), decoderContext);

                errors.addAll(resultOf.getErrors());
                if (resultOf.hasResults()) {
                    results[i] = (T) resultOf.results();
                }

            } else {
                errors.add(new ValidationError.ArrayMissingIndex(i));
                results[i] = null;
            }
        }

        return GResultOf.resultOf(results, errors);
    }
}
