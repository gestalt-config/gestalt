package org.config.gestalt.reload;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.source.ConfigSource;

public interface ConfigReloadListener {
    void reload(ConfigSource source) throws GestaltException;
}
