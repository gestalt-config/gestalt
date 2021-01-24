package org.config.gestalt.decoder;

import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

import java.util.List;

/**
 * Contains all decoders and functionality interact and decode a node.
 *
 * @author Colin Redmond
 */
public interface DecoderService {
    /**
     * Get a list of decoders supported.
     *
     * @return ist of decoders supported
     */
    List<Decoder<?>> getDecoders();

    /**
     * Set a list of decoders, replaces any existing decoders.
     *
     * @param decoders list of decoders
     */
    void setDecoders(List<Decoder<?>> decoders);

    /**
     * Decode a node and return the ValidateOf with the results.
     *
     * @param path Current path we are decoding, used for logging
     * @param configNode the current node we are decoding
     * @param klass the TypeCapture of the node we are decoding
     * @param <T> The generic type of the node we are decoding.
     * @return ValidateOf the code we are decoding.
     */
    <T> ValidateOf<T> decodeNode(String path, ConfigNode configNode, TypeCapture<T> klass);

    /**
     * Add decoders to the service.
     *
     * @param decoder list of decoders
     */
    void addDecoders(List<Decoder<?>> decoder);

    /**
     * Gets the next node in a path for a string.
     *
     * @param path Current path we are decoding, used for logging
     * @param nextString path of the next node
     * @param configNode current config node
     * @return the next config node.
     */
    ValidateOf<ConfigNode> getNextNode(String path, String nextString, ConfigNode configNode);

    /**
     * Gets the next node in a path for a int index.
     *
     * @param path Current path we are decoding, used for logging
     * @param nextIndex path of the next node
     * @param configNode current config node
     * @return the next config node.
     */
    ValidateOf<ConfigNode> getNextNode(String path, int nextIndex, ConfigNode configNode);
}
