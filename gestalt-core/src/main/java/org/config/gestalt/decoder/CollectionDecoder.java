package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ArrayNode;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class for decoding collections. Will handle validation of the node type
 *
 * @param <T> generic type of the collection
 * @author Colin Redmond
 */
public abstract class CollectionDecoder<T extends Collection<?>> implements Decoder<T> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public ValidateOf<T> decode(String path, ConfigNode node, TypeCapture<?> type, DecoderService decoderService) {
        ValidateOf<T> results;
        if (node instanceof ArrayNode) {
            results = arrayDecode(path, node, type, decoderService);
        } else if (node instanceof LeafNode) {
            if (node.getValue().isPresent()) {
                String value = node.getValue().get();
                String[] array = value.split(",");
                List<ConfigNode> leafNodes = Arrays.stream(array).map(String::trim).map(LeafNode::new).collect(Collectors.toList());

                results = arrayDecode(path, new ArrayNode(leafNodes), type, decoderService);
            } else {
                results = ValidateOf.inValid(new ValidationError.DecodingLeafMissingValue(path, node, name()));
            }
        } else {
            results = ValidateOf.inValid(new ValidationError.DecodingExpectedArrayNodeType(path, node, name()));
        }
        return results;
    }

    protected abstract ValidateOf<T> arrayDecode(String path, ConfigNode node, TypeCapture<?> klass, DecoderService decoderService);
}
