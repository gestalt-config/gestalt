package org.config.gestalt.decoder;

import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;

public interface Decoder<T> {

    Priority priority();

    String name();

    boolean matches(TypeCapture<?> klass);

    ValidateOf<T> decode(String path, ConfigNode node, TypeCapture<?> type, DecoderService decoderService);
}
