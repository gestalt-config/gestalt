package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

public abstract class LeafDecoder<T> implements Decoder<T> {

    @Override
    public ValidateOf<T> decode(String path, ConfigNode node, TypeCapture<?> type, DecoderService decoderService) {
        ValidateOf<T> results;
        if (node instanceof LeafNode) {
            if (node.getValue().isPresent()) {
                results = leafDecode(path, node, type);
            } else {
                results = ValidateOf.inValid(new ValidationError.DecodingLeafMissingValue(path, node, name()));
            }
        } else {
            results = ValidateOf.inValid(new ValidationError.DecodingExpectedLeafNodeType(path, node, name()));
        }
        return results;
    }

    protected ValidateOf<T> leafDecode(String path, ConfigNode node, TypeCapture<?> type) {
        return leafDecode(path, node);
    }

    protected abstract ValidateOf<T> leafDecode(String path, ConfigNode node);
}
