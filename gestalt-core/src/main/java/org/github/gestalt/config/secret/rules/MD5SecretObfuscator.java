package org.github.gestalt.config.secret.rules;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Specifies how to obscure a secret by turning it into a MD5 hash.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class MD5SecretObfuscator implements SecretObfuscator {

    private final MessageDigest md = MessageDigest.getInstance("MD5");

    public MD5SecretObfuscator() throws NoSuchAlgorithmException {

    }

    @Override
    public String obfuscator(String value) {
        byte[] messageDigest = md.digest(value.getBytes());

        return convertToHex(messageDigest);
    }

    private String convertToHex(final byte[] messageDigest) {
        BigInteger bigint = new BigInteger(1, messageDigest);
        String hexText = bigint.toString(16);
        while (hexText.length() < 32) {
            hexText = "0".concat(hexText);
        }
        return hexText;
    }
}
