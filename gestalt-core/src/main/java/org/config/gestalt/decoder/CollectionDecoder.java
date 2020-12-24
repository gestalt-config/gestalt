package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ArrayNode;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;
import org.config.gestalt.node.ConfigNode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CollectionDecoder implements Decoder {

    @Override
    public <T> ValidateOf<T> decode(String path, ConfigNode node, TypeCapture<T> type, DecoderService decoderService) {
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

    protected abstract <T> ValidateOf<T> arrayDecode(String path, ConfigNode node, TypeCapture<T> klass, DecoderService decoderService);
}
