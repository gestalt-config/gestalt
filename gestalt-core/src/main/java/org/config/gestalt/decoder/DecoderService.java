package org.config.gestalt.decoder;

import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;
import org.config.gestalt.node.ConfigNode;

import java.util.List;

public interface DecoderService {
    List<Decoder> getDecoders();

    void setDecoders(List<Decoder> decoders);

    <T> ValidateOf<T> decodeNode(String path, ConfigNode configNode, TypeCapture<T> klass);

    void addDecoders(List<Decoder> decoder);
}
