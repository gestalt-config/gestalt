package org.github.gestalt.config.security.encrypted;

import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.NodeType;
import org.github.gestalt.config.secret.rules.SecretConcealer;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

    public static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    public static final int GCM_TAG_LENGTH = 16;
    public static final int GCM_IV_LENGTH = 12;
    private final SecretKey skey;

    private final byte[] encryptedData;

    public EncryptedLeafNode(byte[] encryptedData, SecretKey skey) throws IllegalBlockSizeException, BadPaddingException {
        super("");

        this.skey = skey;
        this.encryptedData = encryptedData;
    }

    @Override
    public Optional<String> getValue() {
        try {
            return Optional.of(decryptGcm(skey, encryptedData));
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

    public static String decryptGcm(SecretKey skey, byte[] ciphertext)
        throws BadPaddingException, IllegalBlockSizeException /* these indicate corrupt or malicious ciphertext */
        /* Note that AEADBadTagException may be thrown in GCM mode; this is a subclass of BadPaddingException */
    {
        /* Precond: skey is valid and GCM mode is available in the JRE;
         * otherwise IllegalStateException will be thrown. */
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] initVector = Arrays.copyOfRange(ciphertext, 0, GCM_IV_LENGTH);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * java.lang.Byte.SIZE, initVector);
            cipher.init(Cipher.DECRYPT_MODE, skey, spec);
            byte[] plaintext = cipher.doFinal(ciphertext, GCM_IV_LENGTH, ciphertext.length - GCM_IV_LENGTH);
            return new String(plaintext);
        } catch (NoSuchPaddingException | InvalidAlgorithmParameterException |
                 InvalidKeyException | NoSuchAlgorithmException e)
        {
            /* None of these exceptions should be possible if precond is met. */
            throw new IllegalStateException(e.toString());
        }
    }
}
