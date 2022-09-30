package org.github.gestalt.config.test.classes;

import org.github.gestalt.config.annotations.Config;

public interface IDBInfoMethodAnnotations {
    @Config(defaultVal = "1234")
    int getPort();

    String getUri();

    String getPassword();
}
