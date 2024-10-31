package org.github.gestalt.config.security.encrypted;

import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.node.NodeType;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EncryptedLeafNodeTest {

    private byte[] encryptedData;
    private SecretKey secretKey;

    public static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    public static final int GCM_TAG_LENGTH = 16;
    public static final int GCM_IV_LENGTH = 12;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize the encryption and decryption ciphers
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        secretKey = keyGenerator.generateKey();

        String originalText = "secretData";

        byte[] ciphertext;
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] initVector = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(initVector);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * java.lang.Byte.SIZE, initVector);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
        byte[] encoded = originalText.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ciphertext = new byte[initVector.length + cipher.getOutputSize(encoded.length)];
        System.arraycopy(initVector, 0, ciphertext, 0, initVector.length);
        // Perform encryption
        cipher.doFinal(encoded, 0, encoded.length, ciphertext, initVector.length);
        encryptedData = ciphertext;
    }

    @Test
    void testGetValue() throws Exception {
        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, secretKey, Map.of());
        assertEquals(Optional.of("secretData"), encryptedLeafNode.getValue());
    }

    @Test
    void testGetValueWithBadPaddingException() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        var newSecretKey = keyGenerator.generateKey();

        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, newSecretKey, Map.of());
        assertEquals(Optional.empty(), encryptedLeafNode.getValue());
    }

    @Test
    void testGetNodeType() throws IllegalBlockSizeException, BadPaddingException {
        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, secretKey, Map.of());
        assertEquals(NodeType.LEAF, encryptedLeafNode.getNodeType());
    }

    @Test
    void testGetIndex() throws IllegalBlockSizeException, BadPaddingException {
        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, secretKey, Map.of());
        assertEquals(Optional.empty(), encryptedLeafNode.getIndex(0));
    }

    @Test
    void testGetKey() throws IllegalBlockSizeException, BadPaddingException {
        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, secretKey, Map.of());
        assertEquals(Optional.empty(), encryptedLeafNode.getKey("key"));
    }

    @Test
    void testSize() throws IllegalBlockSizeException, BadPaddingException {
        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, secretKey, Map.of());
        assertEquals(1, encryptedLeafNode.size());
    }

    @Test
    void testEquals() throws Exception {
        EncryptedLeafNode encryptedLeafNode1 = new EncryptedLeafNode(encryptedData, secretKey, Map.of());
        EncryptedLeafNode encryptedLeafNode2 = new EncryptedLeafNode(encryptedData, secretKey, Map.of());

        assertEquals(encryptedLeafNode1, encryptedLeafNode1);
        assertEquals(encryptedLeafNode1, encryptedLeafNode2);
        assertNotEquals(encryptedLeafNode1, new Object());
    }

    @Test
    void testHashCode() throws Exception {
        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, secretKey, Map.of());
        assertEquals(Arrays.hashCode(encryptedData), encryptedLeafNode.hashCode());
    }

    @Test
    void testPrinter() throws Exception {
        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, secretKey, Map.of());
        SecretConcealer secretConcealer = (path, value, metaData) -> "concealedSecret";

        assertEquals("EncryptedLeafNode{value='concealedSecret'}", encryptedLeafNode.printer("", secretConcealer, new PathLexer()));
    }

    @Test
    void testToString() throws Exception {
        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, secretKey, Map.of());
        assertEquals("EncryptedLeafNode{value='secret'}", encryptedLeafNode.toString());
    }
}
