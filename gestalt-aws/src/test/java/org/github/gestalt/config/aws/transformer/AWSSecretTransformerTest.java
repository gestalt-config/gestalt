package org.github.gestalt.config.aws.transformer;

import org.github.gestalt.config.aws.config.AWSBuilder;
import org.github.gestalt.config.aws.config.AWSModuleConfig;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.post.process.PostProcessorConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.InvalidParameterException;

class AWSSecretTransformerTest {

    final SecretsManagerClientBuilder secretsManagerClientBuilder = Mockito.mock();
    final SecretsManagerClient secretsManagerClient = Mockito.mock();

    @BeforeEach
    public void setup() {
        Mockito.reset(secretsManagerClientBuilder, secretsManagerClient);
    }

    @Test
    void name() {
        AWSSecretTransformer transform = new AWSSecretTransformer();
        Assertions.assertEquals("awsSecret", transform.name());
    }

    @Test
    void process() {
        try (MockedStatic<SecretsManagerClient> secretClient = Mockito.mockStatic(SecretsManagerClient.class)) {
            secretClient.when(SecretsManagerClient::builder).thenReturn(secretsManagerClientBuilder);
            Mockito.when(secretsManagerClientBuilder.region(Region.of("eu-west-3"))).thenReturn(secretsManagerClientBuilder);
            Mockito.when(secretsManagerClientBuilder.credentialsProvider(Mockito.any())).thenReturn(secretsManagerClientBuilder);
            Mockito.when(secretsManagerClientBuilder.httpClient(Mockito.any())).thenReturn(secretsManagerClientBuilder);
            Mockito.when(secretsManagerClientBuilder.build()).thenReturn(secretsManagerClient);

            AWSSecretTransformer transform = new AWSSecretTransformer();
            GestaltConfig gestaltConfig = new GestaltConfig();
            gestaltConfig.registerModuleConfig(new AWSModuleConfig("eu-west-3"));
            PostProcessorConfig config = new PostProcessorConfig(gestaltConfig, null, null);
            transform.applyConfig(config);

            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                                                                      .secretId("secret")
                                                                      .build();

            GetSecretValueResponse getSecretValueResponse = GetSecretValueResponse.builder()
                                                                                  .secretString("{\"mySecret\" : \"hello world\"}")
                                                                                  .build();
            Mockito.when(secretsManagerClient.getSecretValue(valueRequest)).thenReturn(getSecretValueResponse);

            var results = transform.process("test", "secret:mySecret", "awsSecret:secret:mySecret");

            Assertions.assertTrue(results.hasResults());
            Assertions.assertFalse(results.hasErrors());

            Assertions.assertEquals("hello world", results.results());
        }
    }

    @Test
    void processWithSecretClientProvided() throws GestaltConfigurationException {

        AWSBuilder awsConfigExtension = AWSBuilder.builder();
        awsConfigExtension.setRegion("usa");
        AWSModuleConfig awsModuleConfig = awsConfigExtension.build();
        awsModuleConfig.setSecretsClient(secretsManagerClient);

        AWSSecretTransformer transform = new AWSSecretTransformer();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(awsModuleConfig);
        PostProcessorConfig config = new PostProcessorConfig(gestaltConfig, null, null);
        transform.applyConfig(config);

        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                                                                  .secretId("secret")
                                                                  .build();

        GetSecretValueResponse getSecretValueResponse = GetSecretValueResponse.builder()
                                                                              .secretString("{\"mySecret\" : \"hello world\"}")
                                                                              .build();
        Mockito.when(secretsManagerClient.getSecretValue(valueRequest)).thenReturn(getSecretValueResponse);

        var results = transform.process("test", "secret:mySecret", "awsSecret:secret:mySecret");

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals("hello world", results.results());

    }


    @Test
    void processInvalidSecretKeyFormat() {
        try (MockedStatic<SecretsManagerClient> secretClient = Mockito.mockStatic(SecretsManagerClient.class)) {
            secretClient.when(SecretsManagerClient::builder).thenReturn(secretsManagerClientBuilder);
            Mockito.when(secretsManagerClientBuilder.region(Region.of("eu-west-3"))).thenReturn(secretsManagerClientBuilder);
            Mockito.when(secretsManagerClientBuilder.credentialsProvider(Mockito.any())).thenReturn(secretsManagerClientBuilder);
            Mockito.when(secretsManagerClientBuilder.httpClient(Mockito.any())).thenReturn(secretsManagerClientBuilder);
            Mockito.when(secretsManagerClientBuilder.build()).thenReturn(secretsManagerClient);


            AWSSecretTransformer transform = new AWSSecretTransformer();
            GestaltConfig gestaltConfig = new GestaltConfig();
            gestaltConfig.registerModuleConfig(new AWSModuleConfig("eu-west-3"));
            PostProcessorConfig config = new PostProcessorConfig(gestaltConfig, null, null);
            transform.applyConfig(config);

            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                                                                      .secretId("secret")
                                                                      .build();

            GetSecretValueResponse getSecretValueResponse = GetSecretValueResponse.builder()
                                                                                  .secretString("{\"mySecret\" : \"hello world\"}")
                                                                                  .build();
            Mockito.when(secretsManagerClient.getSecretValue(valueRequest)).thenReturn(getSecretValueResponse);

            var results = transform.process("test", "secret", "awsSecret:secret");

            Assertions.assertFalse(results.hasResults());
            Assertions.assertTrue(results.hasErrors());

            Assertions.assertEquals(1, results.getErrors().size());
            Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
            Assertions.assertEquals("AWS Secret must be in the format secretName:SecretKey  received ${awsSecret:secret} " +
                "with parts [secret], on the path: test", results.getErrors().get(0).description());
        }
    }

    @Test
    void processMissingSecretKey() {
        try (MockedStatic<SecretsManagerClient> secretClient = Mockito.mockStatic(SecretsManagerClient.class)) {
            secretClient.when(SecretsManagerClient::builder).thenReturn(secretsManagerClientBuilder);
            Mockito.when(secretsManagerClientBuilder.region(Region.of("eu-west-3"))).thenReturn(secretsManagerClientBuilder);
            Mockito.when(secretsManagerClientBuilder.credentialsProvider(Mockito.any())).thenReturn(secretsManagerClientBuilder);
            Mockito.when(secretsManagerClientBuilder.httpClient(Mockito.any())).thenReturn(secretsManagerClientBuilder);
            Mockito.when(secretsManagerClientBuilder.build()).thenReturn(secretsManagerClient);

            AWSSecretTransformer transform = new AWSSecretTransformer();
            GestaltConfig gestaltConfig = new GestaltConfig();
            gestaltConfig.registerModuleConfig(new AWSModuleConfig("eu-west-3"));
            PostProcessorConfig config = new PostProcessorConfig(gestaltConfig, null, null);
            transform.applyConfig(config);

            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                                                                      .secretId("secret")
                                                                      .build();

            GetSecretValueResponse getSecretValueResponse = GetSecretValueResponse.builder()
                                                                                  .secretString("{\"mySecret\" : \"hello world\"}")
                                                                                  .build();
            Mockito.when(secretsManagerClient.getSecretValue(valueRequest)).thenReturn(getSecretValueResponse);

            var results = transform.process("test", "secret:notASecret", "awsSecret:secret:notASecret");

            Assertions.assertFalse(results.hasResults());
            Assertions.assertTrue(results.hasErrors());

            Assertions.assertEquals(1, results.getErrors().size());
            Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
            Assertions.assertEquals("AWS Secret secret does not have the key notASecret, on the path: test " +
                "with substitution awsSecret:secret:notASecret", results.getErrors().get(0).description());
        }
    }

    @Test
    void noAWSConfigSet() {

        AWSSecretTransformer transform = new AWSSecretTransformer();
        GestaltConfig gestaltConfig = new GestaltConfig();
        PostProcessorConfig config = new PostProcessorConfig(gestaltConfig, null, null);
        transform.applyConfig(config);

        var results = transform.process("test", "secret:myKey", "awsSecret:secret:myKey");
        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("AWSModuleConfig has not been registered. " +
                "Register by creating a AWSBuilder then registering the AWSBuilder.build() results with the Gestalt " +
                "Builder.addModuleConfig(). If you wish to use the aws module with string substitution ${awsSecret:secret:myKey} " +
                "on the path: test",
            results.getErrors().get(0).description());
    }


    @Test
    void processError() {
        try (MockedStatic<SecretsManagerClient> secretClient = Mockito.mockStatic(SecretsManagerClient.class)) {
            secretClient.when(SecretsManagerClient::builder).thenReturn(secretsManagerClientBuilder);
            Mockito.when(secretsManagerClientBuilder.region(Region.of("eu-west-3"))).thenReturn(secretsManagerClientBuilder);
            Mockito.when(secretsManagerClientBuilder.credentialsProvider(Mockito.any())).thenReturn(secretsManagerClientBuilder);
            Mockito.when(secretsManagerClientBuilder.httpClient(Mockito.any())).thenReturn(secretsManagerClientBuilder);
            Mockito.when(secretsManagerClientBuilder.build()).thenReturn(secretsManagerClient);

            AWSSecretTransformer transform = new AWSSecretTransformer();
            GestaltConfig gestaltConfig = new GestaltConfig();
            gestaltConfig.registerModuleConfig(new AWSModuleConfig("eu-west-3"));
            PostProcessorConfig config = new PostProcessorConfig(gestaltConfig, null, null);
            transform.applyConfig(config);

            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                                                                      .secretId("secret")
                                                                      .build();

            Mockito.when(secretsManagerClient.getSecretValue(valueRequest))
                   .thenThrow(InvalidParameterException.builder().requestId("abc").message("error").build());

            var results = transform.process("test", "secret:myKey", "awsSecret:secret:myKey");

            Assertions.assertFalse(results.hasResults());
            Assertions.assertTrue(results.hasErrors());

            Assertions.assertEquals(1, results.getErrors().size());
            Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
            Assertions.assertEquals("Exception thrown while loading AWS secret: awsSecret:secret:myKey, on path: test " +
                "in transformer: awsSecret, with message: error", results.getErrors().get(0).description());
        }
    }

    @Test
    void awsConfigExtension() throws GestaltConfigurationException {
        AWSBuilder awsConfigExtension = AWSBuilder.builder();
        awsConfigExtension.setRegion("usa");
        Assertions.assertEquals("usa", awsConfigExtension.getRegion());
        AWSModuleConfig awsModuleConfig = awsConfigExtension.build();
        Assertions.assertEquals("usa", awsModuleConfig.getRegion());
        Assertions.assertEquals("aws", awsModuleConfig.name());
    }
}
