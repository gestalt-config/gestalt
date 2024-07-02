package org.github.gestalt.config.test.classes;

import jakarta.annotation.Nullable;

public class DBInfoNullableGetter2 {
    private int port;

    private String uri;
    private String password;

    public int port() {
        return port;
    }

    @Nullable
    public String uri() {
        return uri;
    }

    public String password() {
        return password;
    }
}
