package org.config.gestalt.decoder;

import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

import java.util.List;

public interface DecoderService {
    List<Decoder<?>> getDecoders();

    void setDecoders(List<Decoder<?>> decoders);

    <T> ValidateOf<T> decodeNode(String path, ConfigNode configNode, TypeCapture<T> klass);

    void addDecoders(List<Decoder<?>> decoder);

    ValidateOf<ConfigNode> getNextNode(String path, String nextString, ConfigNode configNode);

    ValidateOf<ConfigNode> getNextNode(String path, int nextIndex, ConfigNode configNode);
}
