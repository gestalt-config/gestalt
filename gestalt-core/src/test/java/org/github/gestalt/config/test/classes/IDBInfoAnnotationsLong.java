package org.github.gestalt.config.test.classes;

import org.github.gestalt.config.annotations.Config;

public interface IDBInfoAnnotationsLong {
    @Config(path = "channel.port", defaultVal = "1234")
    int getPort();

    String getUri();

    String getPassword();
}
