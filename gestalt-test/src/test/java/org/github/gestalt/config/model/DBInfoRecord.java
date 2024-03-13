package org.github.gestalt.config.model;

public record DBInfoRecord(int port, String uri, String password, Integer connections) {
}
