package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.ValidateOf;

/**
 * Base class for leaf decoders. Will decode leaf types, including booleans, integers ect.
 *
 * @param <T> generic type for this leaf decoder
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public abstract class LeafDecoder<T> implements Decoder<T> {

    /**
     * Validates that the current node is a leaf and if it is call leafDecode.
     *
     * @param path the current path
     * @param node the current node we are decoding.
     * @param type the type of object we are decoding.
     * @param decoderService decoder Service used to decode members if needed. Such as class fields.
     * @return ValidateOf the current node with details of either success or failures.
     */
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

    /**
     * Decode a leaf value.
     *
     * @param path the current path
     * @param node the current node we are decoding.
     * @param type the type of object we are decoding.
     * @return ValidateOf the current node with details of either success or failures.
     */
    protected ValidateOf<T> leafDecode(String path, ConfigNode node, TypeCapture<?> type) {
        return leafDecode(path, node);
    }

    /**
     * Decode a leaf value.
     *
     * @param path the current path
     * @param node the current node we are decoding.
     * @return ValidateOf the current node with details of either success or failures.
     */
    protected abstract ValidateOf<T> leafDecode(String path, ConfigNode node);
}
