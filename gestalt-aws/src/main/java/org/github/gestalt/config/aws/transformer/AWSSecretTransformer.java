package org.github.gestalt.config.aws.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.gestalt.config.aws.config.AWSModuleConfig;
import org.github.gestalt.config.aws.errors.AWSValidationErrors;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.processor.config.transform.Transformer;
import org.github.gestalt.config.utils.GResultOf;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

/**
 * Allows you to substitute an aws secret using ${awsSecret:secretName:secretKey}.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class AWSSecretTransformer implements Transformer {

    private static final System.Logger logger = System.getLogger(AWSSecretTransformer.class.getName());
    private final ObjectMapper mapper = new ObjectMapper();
    private SecretsManagerClient secretsClient;

    @Override
    public String name() {
        return "awsSecret";
    }

    @Override
    public void applyConfig(ConfigNodeProcessorConfig config) {
        AWSModuleConfig moduleConfig = config.getConfig().getModuleConfig(AWSModuleConfig.class);

        if (moduleConfig == null) {
            logger.log(System.Logger.Level.WARNING, "AWSModuleConfig has not been registered. " +
                "if you wish to use the aws module with string substitution ${awsSecret:key} " +
                "then you must register an AWSModuleConfig config moduleConfig using the builder");
        } else {
            if (moduleConfig.hasSecretsClient()) {
                secretsClient = moduleConfig.getSecretsClient();
            } else if (moduleConfig.getRegion() != null) {
                secretsClient = SecretsManagerClient.builder()
                                                    .region(Region.of(moduleConfig.getRegion()))
                                                    .credentialsProvider(ProfileCredentialsProvider.create())
                                                    .httpClient(UrlConnectionHttpClient.builder().build())
                                                    .build();
            } else {
                logger.log(System.Logger.Level.ERROR, "AWSModuleConfig was registered but neither the secret client " +
                    "nor the region was provided");
            }
        }
    }

    @Override
    public GResultOf<String> process(String path, String secretNameKey, String rawValue) {
        if (secretNameKey != null && !secretNameKey.isEmpty()) {
            try {
                if (secretsClient == null) {
                    return GResultOf.errors(new AWSValidationErrors.AWSModuleConfigNotSet(path, rawValue));
                }

                String[] secretParts = secretNameKey.split(":");

                if (secretParts.length != 2) {
                    return GResultOf.errors(new AWSValidationErrors.AWSSecretInvalid(path, rawValue, secretParts));
                }

                String secretName = secretParts[0];
                String secretKey = secretParts[1];

                GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                                                                          .secretId(secretName)
                                                                          .build();

                GetSecretValueResponse valueResponse = secretsClient.getSecretValue(valueRequest);
                String secret = valueResponse.secretString();
                JsonNode jsonNode = mapper.readTree(secret);

                if (!jsonNode.has(secretKey)) {
                    return GResultOf.errors(new AWSValidationErrors.AWSSecretDoesNotExist(path, secretName, secretKey, rawValue));
                }

                JsonNode secretNode = jsonNode.get(secretKey);

                return GResultOf.result(secretNode.asText());

            } catch (Exception e) {
                return GResultOf.errors(new AWSValidationErrors.ExceptionProcessingAWSSecret(path, rawValue, name(), e));
            }
        } else {
            return GResultOf.errors(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }
    }
}
