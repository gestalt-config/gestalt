package org.github.gestalt.config.security.encrypted;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.metadata.IsEncryptedMetadata;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.processor.config.ConfigNodeProcessor;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.secret.rules.RegexSecretChecker;
import org.github.gestalt.config.secret.rules.SecretChecker;
import org.github.gestalt.config.utils.GResultOf;

import javax.crypto.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.Set;

/**
 * Checks if the node is a leaf and a temporary secret. if it is, replaces the leaf node with a TemporaryLeafNode that can only be accessed
 * a limited number of times. After the limited number of times, the value is released to be GC'ed.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
@ConfigPriority(400)
public class EncryptedSecretConfigNodeProcessor implements ConfigNodeProcessor {

    private static final System.Logger logger = System.getLogger(EncryptedSecretConfigNodeProcessor.class.getName());
    private SecretChecker encryptedSecret = new RegexSecretChecker(Set.of());

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

        var metadata = currentNode.getMetadata();

        // if this is not an encrypted secret node, return the original node.
        if (!encryptedSecret.isSecret(path) && (!metadata.containsKey(IsEncryptedMetadata.ENCRYPTED) || //NOPMD
            metadata.get(IsEncryptedMetadata.ENCRYPTED).stream().noneMatch(it -> (boolean) it.getMetadata()))) {
            return GResultOf.result(currentNode);
        }

        Optional<String> optionalLeafNodeValue = ((LeafNode) currentNode).getValueInternal();

        // if the leaf node doesn't have a value, we don't need to encrypt it.
        if (optionalLeafNodeValue.isEmpty()) {
            return GResultOf.result(currentNode);
        }


        // for each leaf we create a new encryption and decryption cipher.
        // We use the encryption cipher to encrypt the data and pass the encrypted data along with the
        // decryption cipher to the leaf.
        try {
            var secretKey = EncryptionUtils.generateKey(128);
            var encryptedData = EncryptionUtils.encryptGcm(secretKey, optionalLeafNodeValue.orElse(""));

            return GResultOf.result(new EncryptedLeafNode(encryptedData, secretKey, metadata));

        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException |
                 InvalidAlgorithmParameterException | InvalidKeyException | ShortBufferException ex) {
            return GResultOf.errors(new ValidationError.EncryptedNodeFailure(path, ex));
        }
    }
}
