package org.github.gestalt.config.google.storage.transformer;

import com.google.cloud.ServiceOptions;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.google.storage.errors.ExceptionProcessingGCPSecret;
import org.github.gestalt.config.post.process.transform.Transformer;
import org.github.gestalt.config.utils.ValidateOf;

/**
 * Allows you to substitute a GCP secret.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class GCPSecretTransformer implements Transformer {
    @Override
    public String name() {
        return "gcpSecret";
    }

    @Override
    public ValidateOf<String> process(String path, String key, String rawValue) {
        if (key != null) {
            try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
                String projectId = ServiceOptions.getDefaultProjectId();
                SecretVersionName secretVersionName = SecretVersionName.of(projectId, key, "latest");

                AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);

                String secret = response.getPayload().getData().toStringUtf8();

                return ValidateOf.valid(secret);
            } catch (Exception ex) {
                return ValidateOf.inValid(new ExceptionProcessingGCPSecret(path, key, name(), ex));
            }
        } else {
            return ValidateOf.inValid(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }
    }
}
