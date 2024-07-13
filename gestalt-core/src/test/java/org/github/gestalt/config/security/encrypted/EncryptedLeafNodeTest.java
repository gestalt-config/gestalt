package org.github.gestalt.config.security.encrypted;

import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.node.NodeType;
import org.github.gestalt.config.secret.rules.SecretConcealer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EncryptedLeafNodeTest {

    private Cipher decryptCipher;
    private byte[] encryptedData;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize the encryption and decryption ciphers
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey secretKey = keyGenerator.generateKey();

        Cipher encryptCipher = Cipher.getInstance("AES");
        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);

        decryptCipher = Cipher.getInstance("AES");
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);

        // Encrypt sample data
        String originalText = "secretData";
        encryptedData = encryptCipher.doFinal(originalText.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void testGetValue() throws Exception {
        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, decryptCipher);
        assertEquals(Optional.of("secretData"), encryptedLeafNode.getValue());
    }

    @Test
    void testGetValueWithBadPaddingException() throws Exception {
        Cipher invalidDecryptCipher = Cipher.getInstance("AES");
        invalidDecryptCipher.init(Cipher.DECRYPT_MODE, KeyGenerator.getInstance("AES").generateKey());

        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, invalidDecryptCipher);
        assertEquals(Optional.empty(), encryptedLeafNode.getValue());
    }

    @Test
    void testGetNodeType() throws IllegalBlockSizeException, BadPaddingException {
        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, decryptCipher);
        assertEquals(NodeType.LEAF, encryptedLeafNode.getNodeType());
    }

    @Test
    void testGetIndex() throws IllegalBlockSizeException, BadPaddingException {
        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, decryptCipher);
        assertEquals(Optional.empty(), encryptedLeafNode.getIndex(0));
    }

    @Test
    void testGetKey() throws IllegalBlockSizeException, BadPaddingException {
        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, decryptCipher);
        assertEquals(Optional.empty(), encryptedLeafNode.getKey("key"));
    }

    @Test
    void testSize() throws IllegalBlockSizeException, BadPaddingException {
        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, decryptCipher);
        assertEquals(1, encryptedLeafNode.size());
    }

    @Test
    void testEquals() throws Exception {
        EncryptedLeafNode encryptedLeafNode1 = new EncryptedLeafNode(encryptedData, decryptCipher);
        EncryptedLeafNode encryptedLeafNode2 = new EncryptedLeafNode(encryptedData, decryptCipher);

        assertEquals(encryptedLeafNode1, encryptedLeafNode1);
        assertEquals(encryptedLeafNode1, encryptedLeafNode2);
        assertNotEquals(encryptedLeafNode1, new Object());
    }

    @Test
    void testHashCode() throws Exception {
        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, decryptCipher);
        assertEquals(Arrays.hashCode(encryptedData), encryptedLeafNode.hashCode());
    }

    @Test
    void testPrinter() throws Exception {
        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, decryptCipher);
        SecretConcealer secretConcealer = (path, value) -> "concealedSecret";

        assertEquals("EncryptedLeafNode{value='concealedSecret'}", encryptedLeafNode.printer("", secretConcealer, new PathLexer()));
    }

    @Test
    void testToString() throws Exception {
        EncryptedLeafNode encryptedLeafNode = new EncryptedLeafNode(encryptedData, decryptCipher);
        assertEquals("EncryptedLeafNode{value='secret'}", encryptedLeafNode.toString());
    }
}
