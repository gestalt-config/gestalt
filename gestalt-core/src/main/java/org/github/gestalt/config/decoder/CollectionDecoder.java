package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ArrayNode;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class for decoding collections. Will handle validation of the node type.
 *
 * @param <T> generic type of the collection
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public abstract class CollectionDecoder<T extends Collection<?>> implements Decoder<T> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public GResultOf<T> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {
        GResultOf<T> results;
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
     * @param tags           Current tags for the request
     * @param node           current node we are decoding
     * @param klass          class to decode into
     * @param decoderContext The context of the current decoder.
     * @return GResultOf array built from the config node
     */
    protected abstract GResultOf<T> arrayDecode(String path, Tags tags, ConfigNode node,
                                                TypeCapture<?> klass, DecoderContext decoderContext);
}
