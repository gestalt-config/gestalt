package org.github.gestalt.config.aws.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.gestalt.config.aws.config.AWSModuleConfig;
import org.github.gestalt.config.aws.errors.AWSValidationErrors;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.post.process.PostProcessorConfig;
import org.github.gestalt.config.post.process.transform.Transformer;
import org.github.gestalt.config.utils.ValidateOf;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

/**
 * Allows you to substitute an aws secret using ${awsSecret:secretName:secretKey}.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class AWSSecretTransformer implements Transformer {

    private static final System.Logger logger = System.getLogger(AWSSecretTransformer.class.getName());
    private final ObjectMapper mapper = new ObjectMapper();
    private SecretsManagerClient secretsClient;

    @Override
    public String name() {
        return "awsSecret";
    }

    @Override
    public void applyConfig(PostProcessorConfig config) {
        AWSModuleConfig extension = config.getConfig().getModuleConfig(AWSModuleConfig.class);

        if (extension == null) {
            logger.log(System.Logger.Level.WARNING, "AWSConfigExtension has not been registered. " +
                "if you wish to use the aws module with string substitution ${awsSecret:key} " +
                "then you must register an AWSConfigExtension config extension using the builder");
        } else {
            secretsClient = SecretsManagerClient.builder()
                                                .region(Region.of(extension.getRegion()))
                                                .credentialsProvider(ProfileCredentialsProvider.create())
                                                .httpClient(UrlConnectionHttpClient.builder().build())
                                                .build();
        }
    }

    @Override
    public ValidateOf<String> process(String path, String secretNameKey, String rawValue) {
        if (secretNameKey != null) {
            try {
                String[] secretParts = secretNameKey.split(":");

                if (secretParts.length != 2) {
                    return ValidateOf.inValid(new AWSValidationErrors.AWSSecretInvalid(path, rawValue, secretParts));
                }

                String secretName = secretParts[0];
                String secretKey = secretParts[1];

                if (secretsClient == null) {
                    return ValidateOf.inValid(new AWSValidationErrors.AWSExtensionConfigNotSet(path, rawValue));
                }

                GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                                                                          .secretId(secretName)
                                                                          .build();

                GetSecretValueResponse valueResponse = secretsClient.getSecretValue(valueRequest);
                String secret = valueResponse.secretString();
                JsonNode jsonNode = mapper.readTree(secret);

                if (!jsonNode.has(secretKey)) {
                    return ValidateOf.inValid(new AWSValidationErrors.AWSSecretDoesNotExist(path, secretName, secretKey, rawValue));
                }

                JsonNode secretNode = jsonNode.get(secretKey);

                return ValidateOf.valid(secretNode.asText());

            } catch (Exception e) {
                return ValidateOf.inValid(new AWSValidationErrors.ExceptionProcessingAWSSecret(path, rawValue, name(), e));
            }
        } else {
            return ValidateOf.inValid(new ValidationError.InvalidStringSubstitutionPostProcess(path, rawValue, name()));
        }
    }
}
