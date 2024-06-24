package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

/**
 * Base class for leaf decoders. Will decode leaf types, including booleans, integers ect.
 *
 * @param <T> generic type for this leaf decoder
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public abstract class LeafDecoder<T> implements Decoder<T> {

    /**
     * Validates that the current node is a leaf and if it is call leafDecode.
     *
     * @param path           the current path.
     * @param tags           the tags for the current request.
     * @param node           the current node we are decoding.
     * @param type           the type of object we are decoding.
     * @param decoderContext the information needed to decode an object.
     * @return GResultOf the current node with details of either success or failures.
     */
    @Override
    public GResultOf<T> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {
        GResultOf<T> results;
        if (node instanceof LeafNode) {
            if (node.hasValue()) {
                results = leafDecode(path, node, type, decoderContext);
            } else {
                results = GResultOf.errors(new ValidationError.DecodingLeafMissingValue(path, name()));
            }
        } else {
            results = GResultOf.errors(new ValidationError.DecodingExpectedLeafNodeType(path, node, name()));
        }
        return results;
    }

    /**
     * Decode a leaf value.
     *
     * @param path           the current path.
     * @param node           the current node we are decoding.
     * @param type           the type of object we are decoding.
     * @param decoderContext the decoder contest for this decode
     * @return GResultOf the current node with details of either success or failures.
     */
    protected GResultOf<T> leafDecode(String path, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {
        return leafDecode(path, node, decoderContext);
    }

    /**
     * Decode a leaf value.
     *
     * @param path           the current path.
     * @param node           the current node we are decoding.
     * @param decoderContext the decoder contest for this decode
     * @return GResultOf the current node with details of either success or failures.
     */
    protected abstract GResultOf<T> leafDecode(String path, ConfigNode node, DecoderContext decoderContext);
}
