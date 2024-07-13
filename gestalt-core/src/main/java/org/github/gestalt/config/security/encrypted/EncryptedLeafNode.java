package org.github.gestalt.config.security.encrypted;

import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.NodeType;
import org.github.gestalt.config.secret.rules.SecretConcealer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Temporary leaf node that holds a decorated leaf node.
 * Once the leaf value has been read the accessCount times, it will release the decorated node by setting it to null.
 * Eventually the decorated node should be garbage collected. but while waiting for GC it may still be found in memory.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class EncryptedLeafNode extends LeafNode {
    private final Cipher decryptCipher;

    private final byte[] encryptedData;

    public EncryptedLeafNode(byte[] encryptedData, Cipher decryptCipher) throws IllegalBlockSizeException, BadPaddingException {
        super("");

        this.decryptCipher = decryptCipher;
        this.encryptedData = encryptedData;
    }

    @Override
    public Optional<String> getValue() {
        try {
            return Optional.of(new String(decryptCipher.doFinal(encryptedData)));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            return Optional.empty();
        }
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.LEAF;
    }

    @Override
    public Optional<ConfigNode> getIndex(int index) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigNode> getKey(String key) {
        return Optional.empty();
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EncryptedLeafNode)) {
            return false;
        }
        EncryptedLeafNode leafNode = (EncryptedLeafNode) o;
        return Objects.equals(getValue(), leafNode.getValue());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(encryptedData);
    }

    @Override
    public String toString() {
        // should not be used.
        return printer("", null, new PathLexer());
    }

    @Override
    public String printer(String path, SecretConcealer secretConcealer, SentenceLexer lexer) {
        String nodeValue  = "secret";

        if (secretConcealer != null) {
            nodeValue = secretConcealer.concealSecret(path, nodeValue);
        }

        return "EncryptedLeafNode{" +
            "value='" + nodeValue + '\'' +
            "}";
    }
}
