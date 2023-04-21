package org.github.gestalt.config.post.process.transform.substitution;

import java.util.List;

/**
 * Nodes to build a substitution tree.
 *
 * @author Colin Redmond (c) 2023.
 */
public class SubstitutionNode {

    public static class TextNode extends SubstitutionNode {
        private final String text;

        public TextNode(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    public static class TransformNode extends SubstitutionNode {
        private final List<SubstitutionNode> subNodes;

        public TransformNode(List<SubstitutionNode> subNodes) {
            this.subNodes = subNodes;
        }

        public List<SubstitutionNode> getSubNodes() {
            return subNodes;
        }
    }
}
