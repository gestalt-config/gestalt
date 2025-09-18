package org.github.gestalt.config.security.encrypted;

import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.metadata.MetaDataValue;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.NodeType;
import org.github.gestalt.config.secret.rules.SecretConcealer;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Temporary leaf node that holds a decorated leaf node.
 * Once the leaf value has been read the accessCount times, it will release the decorated node by setting it to null.
 * Eventually the decorated node should be garbage collected. but while waiting for GC it may still be found in memory.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class EncryptedLeafNode extends LeafNode {

    private static final System.Logger logger = System.getLogger(EncryptedSecretConfigNodeProcessor.class.getName());

    public static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    public static final int GCM_TAG_LENGTH = 16;
    public static final int GCM_IV_LENGTH = 12;
    private final SecretKey skey;

    private final byte[] encryptedData;

    public EncryptedLeafNode(byte[] encryptedData, SecretKey skey, Map<String, List<MetaDataValue<?>>> metaData)
        throws IllegalBlockSizeException, BadPaddingException {
        super("", metaData);

        this.skey = skey;
        this.encryptedData = encryptedData;
    }

    /**
     * Duplicate the encrypted node and generate a new encrypted node with the new value.
     *
     * @param value new value for leaf
     * @return new non-encrypted leaf.
     */
    @Override
    public LeafNode duplicate(String value) {
        try {
            var secretKey = EncryptionUtils.generateKey(128);
            var encryptedData = EncryptionUtils.encryptGcm(secretKey, value);

            return new EncryptedLeafNode(encryptedData, secretKey, metadata);
        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException |
                 InvalidAlgorithmParameterException | InvalidKeyException | ShortBufferException ex) {
            logger.log(System.Logger.Level.ERROR, "Exception duplicating EncryptedLeafNode with error " + ex.getMessage() +
                " returning normal leaf");

            return new LeafNode(value, metadata);
        }
    }

    public static String decryptGcm(SecretKey skey, byte[] ciphertext)
        throws BadPaddingException, IllegalBlockSizeException /* these indicate corrupt or malicious ciphertext */
        /* Note that AEADBadTagException may be thrown in GCM mode; this is a subclass of BadPaddingException */ {
        /* Precond: skey is valid and GCM mode is available in the JRE;
         * otherwise IllegalStateException will be thrown. */
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] initVector = Arrays.copyOfRange(ciphertext, 0, GCM_IV_LENGTH);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, initVector);
            cipher.init(Cipher.DECRYPT_MODE, skey, spec);
            byte[] plaintext = cipher.doFinal(ciphertext, GCM_IV_LENGTH, ciphertext.length - GCM_IV_LENGTH);
            return new String(plaintext, Charset.defaultCharset());
        } catch (NoSuchPaddingException | InvalidAlgorithmParameterException |
                 InvalidKeyException | NoSuchAlgorithmException e) {
            /* None of these exceptions should be possible if precond is met. */
            throw new IllegalStateException(e.toString());
        }
    }

    @Override
    public Optional<String> getValue() {
        try {
            return Optional.of(decryptGcm(skey, encryptedData));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            return Optional.empty();
        }
    }

    /**
     * Since we need to decrypt the data, still go through the standard getValue.
     *
     * @return the value for the node decrypted.
     */
    @Override
    public Optional<String> getValueInternal() {
        return getValue();
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
        String nodeValue = "secret";

        if (secretConcealer != null) {
            nodeValue = secretConcealer.concealSecret(path, nodeValue, metadata);
        }

        return "EncryptedLeafNode{" +
            "value='" + nodeValue + '\'' +
            "}";
    }
}
