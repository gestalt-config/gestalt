package org.github.gestalt.config.node;

/**
 * Enumeration of all valid node types.
 *
 * @author Colin Redmond
 */
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
