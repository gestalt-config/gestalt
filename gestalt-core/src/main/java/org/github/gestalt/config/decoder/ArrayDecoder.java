package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ArrayNode;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.PathUtil;
import org.github.gestalt.config.utils.ValidateOf;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Decodes primitive arrays of any type.
 *
 * @param <T> type of array
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
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
    public boolean matches(TypeCapture<?> type) {
        return type.isArray();
    }

    @Override
    public ValidateOf<T[]> decode(String path, ConfigNode node, TypeCapture<?> type, DecoderService decoderService) {
        ValidateOf<T[]> results;
        if (node instanceof ArrayNode) {
            if (node.size() > 0) {
                results = arrayDecode(path, node, type, decoderService);
            } else {
                results = ValidateOf.inValid(new ValidationError.DecodingArrayMissingValue(path, name()));
            }
        } else if (node instanceof LeafNode) {
            if (node.getValue().isPresent()) {
                String value = node.getValue().get();
                String[] array = value.split("(?<!\\\\),");
                List<ConfigNode> leafNodes = Arrays.stream(array)
                                                   .map(String::trim)
                                                   .map(it -> it.replace("\\,", ","))
                                                   .map(LeafNode::new)
                                                   .collect(Collectors.toList());

                results = arrayDecode(path, new ArrayNode(leafNodes), type, decoderService);
            } else {
                results = ValidateOf.inValid(new ValidationError.DecodingLeafMissingValue(path, name()));
            }
        } else {
            results = ValidateOf.inValid(new ValidationError.DecodingExpectedArrayNodeType(path, node, name()));
        }
        return results;
    }

    /**
     * Decode an array values.
     *
     * @param path Current path we are decoding
     * @param node current node we are decoding
     * @param klass class to decode into
     * @param decoderService decoder service use to recursively decode nodes
     * @return ValidateOf array built from the config node
     */
    @SuppressWarnings("unchecked")
    private ValidateOf<T[]> arrayDecode(String path, ConfigNode node, TypeCapture<?> klass, DecoderService decoderService) {
        List<ValidationError> errors = new ArrayList<>();
        T[] results = (T[]) Array.newInstance(klass.getComponentType(), node.size());

        for (int i = 0; i < node.size(); i++) {
            if (node.getIndex(i).isPresent()) {
                ConfigNode currentNode = node.getIndex(i).get();
                String nextPath = PathUtil.pathForIndex(path, i);
                ValidateOf<?> validateOf = decoderService.decodeNode(nextPath, currentNode, TypeCapture.of(klass.getComponentType()));

                errors.addAll(validateOf.getErrors());
                if (validateOf.hasResults()) {
                    results[i] = (T) validateOf.results();
                }

            } else {
                errors.add(new ValidationError.ArrayMissingIndex(i));
                results[i] = null;
            }
        }

        return ValidateOf.validateOf(results, errors);
    }
}
