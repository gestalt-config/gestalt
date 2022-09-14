package org.github.gestalt.config.test.classes;

import org.github.gestalt.config.annotations.Config;

public interface IDBInfoBadAnnotations {
    @Config(path = "channel", defaultVal = "abc")
    int getPort();

    String getUri();

    String getPassword();
}
