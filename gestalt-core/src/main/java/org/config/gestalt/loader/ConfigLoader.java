package org.config.gestalt.loader;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.source.ConfigSource;
import org.config.gestalt.utils.ValidateOf;

public interface ConfigLoader {
    String name();

    boolean accepts(String format);

    ValidateOf<ConfigNode> loadSource(ConfigSource source) throws GestaltException;
}
