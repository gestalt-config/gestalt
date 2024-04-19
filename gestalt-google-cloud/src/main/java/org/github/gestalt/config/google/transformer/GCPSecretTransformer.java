package org.github.gestalt.config.google.transformer;

import com.google.cloud.ServiceOptions;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.google.config.GoogleModuleConfig;
import org.github.gestalt.config.google.errors.ExceptionProcessingGCPSecret;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.processor.config.transform.Transformer;
import org.github.gestalt.config.utils.GResultOf;

/**
 * Allows you to substitute a GCP secret using ${gcpSecret:key}.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class GCPSecretTransformer implements Transformer {

    private String projectId;

    @Override
    public String name() {
        return "gcpSecret";
    }

    @Override
    public void applyConfig(ConfigNodeProcessorConfig config) {
        GoogleModuleConfig moduleConfig = config.getConfig().getModuleConfig(GoogleModuleConfig.class);

        // get the project id from the module config, or use the default
        if (moduleConfig != null) {
            String configProjectId = moduleConfig.getProjectId();
            if (configProjectId != null) {
                projectId = configProjectId;
            } else {
                projectId = ServiceOptions.getDefaultProjectId();
            }
        } else {
            projectId = ServiceOptions.getDefaultProjectId();
        }
    }

    @Override
    public GResultOf<String> process(String path, String key, String rawValue) {
        if (key != null) {
            try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
                SecretVersionName secretVersionName = SecretVersionName.of(projectId, key, "latest");

                AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);

                String secret = response.getPayload().getData().toStringUtf8();

                return GResultOf.result(secret);
            } catch (Exception ex) {
                return GResultOf.errors(new ExceptionProcessingGCPSecret(path, key, name(), ex));
            }
        } else {
            return GResultOf.errors(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }
    }
}
