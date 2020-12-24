package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

public abstract class LeafDecoder implements Decoder {

    @Override
    @SuppressWarnings("unchecked")
    public <T> ValidateOf decode(String path, ConfigNode node, TypeCapture<T> type, DecoderService decoderService) {
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

    protected <T> ValidateOf<T> leafDecode(String path, ConfigNode node, TypeCapture<T> type) {
        return leafDecode(path, node);
    }

    protected abstract <T> ValidateOf<T> leafDecode(String path, ConfigNode node);
}
