package org.config.gestalt.node;

public enum NodeType {
    ARRAY("array"),
    MAP("map"),
    LEAF("leaf");

    private final String type;

    NodeType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
