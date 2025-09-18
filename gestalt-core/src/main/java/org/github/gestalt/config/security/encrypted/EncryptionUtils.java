package org.github.gestalt.config.security.encrypted;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class EncryptionUtils {
    public static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    public static final int GCM_TAG_LENGTH = 16;
    public static final int GCM_IV_LENGTH = 12;

    private EncryptionUtils() {

    }

    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        return keyGenerator.generateKey();
    }

    public static byte[] encryptGcm(SecretKey skey, String plaintext) throws NoSuchPaddingException, NoSuchAlgorithmException,
        InvalidAlgorithmParameterException, InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        /* Precond: skey is valid and GCM mode is available in the JRE;
         * otherwise IllegalStateException will be thrown. */
        byte[] ciphertext;
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        byte[] initVector = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(initVector);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, initVector);
        cipher.init(Cipher.ENCRYPT_MODE, skey, spec);
        byte[] encoded = plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ciphertext = new byte[initVector.length + cipher.getOutputSize(encoded.length)];
        System.arraycopy(initVector, 0, ciphertext, 0, initVector.length);
        // Perform encryption
        cipher.doFinal(encoded, 0, encoded.length, ciphertext, initVector.length);
        return ciphertext;
    }
}
