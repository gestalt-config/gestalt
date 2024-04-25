package org.github.gestalt.config.secret.rules;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

class MD5SecretObfuscatorTest {

    @Test
    void obfuscator() throws NoSuchAlgorithmException {

        MD5SecretObfuscator md5SecretObfuscator = new MD5SecretObfuscator();
        Assertions.assertEquals("482c811da5d5b4bc6d497ffa98491e38", md5SecretObfuscator.obfuscator("password123"));
    }
}
