package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

/**
 * Interface for decoders so we can tell which classes this decoder supports and functionality to decode them.
 *
 * @param <T> the generic type of the decoder.
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public interface Decoder<T> {

    /**
     * Priority for the decoder. Allows us to sort encoders when we have multiple matches.
     *
     * @return Priority
     */
    Priority priority();

    /**
     * Name of the encoder.
     *
     * @return encoder name
     */
    String name();

    /**
     * Apply the GestaltConfig to the Decoder. Needed when building via the ServiceLoader
     * It is a default method as most Decoders don't need to apply configs.
     *
     * @param config GestaltConfig to update the ConfigLoader
     */
    default void applyConfig(GestaltConfig config) {
    }

    /**
     * true if this decoder matches the type capture.
     *
     * @param path the current path
     * @param tags the tags for the current request
     * @param node the current node we are decoding.
     * @param type the type of object we are decoding.
     * @return true if this decoder matches the type capture
     */
    boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type);

    /**
     * Decode the current node. If the current node is a class or list we may need to decode sub nodes.
     *
     * @param path           the current path
     * @param tags           the tags for the current request
     * @param node           the current node we are decoding.
     * @param type           the type of object we are decoding.
     * @param decoderContext The context of the current decoder.
     * @return GResultOf the current node with details of either success or failures.
     */
    GResultOf<T> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext);
}
