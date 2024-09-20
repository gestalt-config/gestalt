package org.github.gestalt.config.aws.builder;


import org.github.gestalt.config.aws.config.AWSBuilder;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

class AWSBuilderTest {

    @Test
    public void createAWSConfig() throws GestaltConfigurationException {

        AWSBuilder builder = AWSBuilder.builder()
                                       .setRegion("test");

        Assertions.assertEquals("test", builder.getRegion());
        Assertions.assertNull(builder.getSecretsClient());
        Assertions.assertEquals("test", builder.build().getRegion());
    }

    @Test
    public void createAWSConfigClient() throws GestaltConfigurationException {
        SecretsManagerClient client = Mockito.mock();

        AWSBuilder builder = AWSBuilder.builder()
                                       .setSecretsClient(client);

        Assertions.assertNull(builder.getRegion());
        Assertions.assertNotNull(builder.getSecretsClient());
        Assertions.assertNull(builder.build().getRegion());
        Assertions.assertNotNull(builder.build().getSecretsClient());
    }
}
