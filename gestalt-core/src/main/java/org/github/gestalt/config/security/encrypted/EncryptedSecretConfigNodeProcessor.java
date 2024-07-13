package org.github.gestalt.config.security.encrypted;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.processor.config.ConfigNodeProcessor;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.secret.rules.RegexSecretChecker;
import org.github.gestalt.config.secret.rules.SecretChecker;
import org.github.gestalt.config.utils.GResultOf;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Set;

/**
 * Checks if the node is a leaf and a temporary secret. if it is, replaces the leaf node with a TemporaryLeafNode that can only be accessed
 * a limited number of times. After the limited number of times, the value is released to be GC'ed.
 *
 *  @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
@ConfigPriority(400)
public class EncryptedSecretConfigNodeProcessor implements ConfigNodeProcessor {

    public static final String AES_CBC_PKCS_5_PADDING = "AES/CBC/PKCS5Padding";
    private SecretChecker encryptedSecret = new RegexSecretChecker(Set.of());

    private static final System.Logger logger = System.getLogger(EncryptedSecretConfigNodeProcessor.class.getName());

    @Override
    public void applyConfig(ConfigNodeProcessorConfig config) {
        EncryptedSecretModule moduleConfig = config.getConfig().getModuleConfig(EncryptedSecretModule.class);

        if (moduleConfig == null) {
            logger.log(System.Logger.Level.DEBUG, "TemporarySecretModule has not been registered. " +
                "if you wish to use the TemporarySecretConfigNodeProcessor " +
                "then you must register an TemporarySecretModule config moduleConfig using the builder");
        } else {
            encryptedSecret = moduleConfig.getSecretChecker();
        }
    }

    @Override
    public GResultOf<ConfigNode> process(String path, ConfigNode currentNode) {
        var valueOptional = currentNode.getValue();
        if (!(currentNode instanceof LeafNode) || valueOptional.isEmpty()) {
            return GResultOf.result(currentNode);
        }

        // if this is not a temporary secret node, return the original node.
        if (!encryptedSecret.isSecret(path)) {
            return GResultOf.result(currentNode);
        }
        Optional<String> optionalLeafNodeValue = currentNode.getValue();

        // if the leaf node doesn't have a value, we don't need to encrypt it.
        if (optionalLeafNodeValue.isEmpty()) {
            return GResultOf.result(currentNode);
        }


        // for each leaf we create a new encryption and decryption cipher.
        // We use the encryption cipher to encrypt the data and pass the encrypted data along with the
        // decryption cipher to the leaf.
        try {
            SecretKey secretKey = generateKey(256);
            IvParameterSpec iv = generateIv();

            Cipher encryptCipher = Cipher.getInstance(AES_CBC_PKCS_5_PADDING);
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

            Cipher decryptCipher = Cipher.getInstance(AES_CBC_PKCS_5_PADDING);
            decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

            return GResultOf.result(new EncryptedLeafNode(encryptCipher.doFinal(optionalLeafNodeValue.orElse("").getBytes()),
                decryptCipher));

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException |
                 InvalidAlgorithmParameterException ex) {
            return GResultOf.errors(new ValidationError.EncryptedNodeFailure(path, ex));
        }
    }

    private static  IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    private static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        return keyGenerator.generateKey();
    }
}
