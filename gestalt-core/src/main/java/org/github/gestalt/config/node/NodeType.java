package org.github.gestalt.config.node;

/**
 * Enumeration of all valid node types.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public enum NodeType {
    /**
     * Array node type.
     */
    ARRAY("array"),
    /**
     * Map node type.
     */
    MAP("map"),
    /**
     * Leaf node type.
     */
    LEAF("leaf");

    private final String type;

    NodeType(String type) {
        this.type = type;
    }

    /**
     * Get the type of this node.
     *
     * @return Get the type of this node
     */
    public String getType() {
        return type;
    }
}
