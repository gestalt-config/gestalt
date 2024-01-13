package org.github.gestalt.config.decoder;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.path.mapper.PathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.List;

/**
 * Contains all decoders and functionality interact and decode a node.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
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
     * Decode a node and return the GResultOf with the results.
     *
     * @param <T>            The generic type of the node we are decoding.
     * @param path           Current path we are decoding, used for logging
     * @param tags           the tags for the current request
     * @param configNode     the current node we are decoding
     * @param klass          the TypeCapture of the node we are decoding
     * @param decoderContext The context for the decoder
     * @return GResultOf the code we are decoding.
     */
    <T> GResultOf<T> decodeNode(String path, Tags tags, String configNode, TypeCapture<T> klass, DecoderContext decoderContext);

    /**
     * Decode a node and return the GResultOf with the results.
     *
     * @param <T>            The generic type of the node we are decoding.
     * @param path           Current path we are decoding, used for logging
     * @param tags           the tags for the current request
     * @param configNode     the current node we are decoding
     * @param klass          the TypeCapture of the node we are decoding
     * @param decoderContext The context for the decoder
     * @return GResultOf the code we are decoding.
     */
    <T> GResultOf<T> decodeNode(String path, Tags tags, ConfigNode configNode, TypeCapture<T> klass, DecoderContext decoderContext);

    /**
     * Add decoders to the service.
     *
     * @param decoder list of decoders
     */
    void addDecoders(List<Decoder<?>> decoder);

    /**
     * Get all path mappers the decoder service has registered.
     *
     * @return PathMapper
     */
    List<PathMapper> getPathMappers();

    /**
     * Set all path mappers the decoder service can use.
     *
     * @param pathMappers path mappers to replace the current ones
     */
    void setPathMappers(List<PathMapper> pathMappers);

    /**
     * Gets the next node from a config node, in a path for a string.
     *
     * @param path       Current path we are decoding, used for logging
     * @param nextPath   path of the next node
     * @param configNode current config node
     * @return the next config node.
     */
    GResultOf<ConfigNode> getNextNode(String path, String nextPath, ConfigNode configNode);

    /**
     * Gets the next node from a config node, in a path for a int index.
     *
     * @param path       Current path we are decoding, used for logging
     * @param nextIndex  path of the next node
     * @param configNode current config node
     * @return the next config node.
     */
    GResultOf<ConfigNode> getNextNode(String path, int nextIndex, ConfigNode configNode);
}
