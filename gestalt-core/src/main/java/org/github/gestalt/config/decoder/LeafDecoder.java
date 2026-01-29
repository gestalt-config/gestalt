package org.github.gestalt.config.decoder;

import java.util.Collections;
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
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
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
                // When the value is an empty string and treatEmptyStringAsAbsent is enabled, return null to preserve field defaults.
                // This allows POJO fields to retain their default values when the configuration contains an empty string
                // where an empty string would otherwise override the default value.
                // Without this, fields would always be set to empty string values instead of keeping their defaults.
                if (((LeafNode) node).getValueInternal().map(String::isEmpty).orElse(false)
                    && decoderContext.getGestaltConfig() != null
                    && decoderContext.getGestaltConfig().isTreatEmptyStringAsAbsent()) {
                    results = GResultOf.resultOf(null, Collections.emptyList());
                } else {
                    results = leafDecode(path, node, type, decoderContext);
                }
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
