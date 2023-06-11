package org.github.gestalt.config.google.transformer;

import com.google.api.gax.httpjson.HttpJsonStatusCode;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.ServiceOptions;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.protobuf.ByteString;
import org.github.gestalt.config.entity.ValidationLevel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

class GCPSecretTransformerTest {

    private final SecretManagerServiceClient secretManagerServiceClient = Mockito.mock();
    private final AccessSecretVersionResponse accessSecretVersionResponse = Mockito.mock();
    private final SecretPayload secretPayload = Mockito.mock();

    @BeforeEach
    void setup() {
        Mockito.reset(secretManagerServiceClient, accessSecretVersionResponse, secretPayload);
    }

    @Test
    void name() {
        GCPSecretTransformer gcpSecretTransformer = new GCPSecretTransformer();
        Assertions.assertEquals("gcpSecret", gcpSecretTransformer.name());
    }

    @Test
    void process() throws IOException {

        try (MockedStatic<SecretManagerServiceClient> secretClient = Mockito.mockStatic(SecretManagerServiceClient.class);
             MockedStatic<ServiceOptions> serviceOptions = Mockito.mockStatic(ServiceOptions.class)) {
            secretClient.when(SecretManagerServiceClient::create).thenReturn(secretManagerServiceClient);
            serviceOptions.when(ServiceOptions::getDefaultProjectId).thenReturn("testProject");

            SecretVersionName secretVersionName = SecretVersionName.of("testProject", "gestalt-secret", "latest");
            Mockito.when(secretManagerServiceClient.accessSecretVersion(secretVersionName)).thenReturn(accessSecretVersionResponse);
            Mockito.when(accessSecretVersionResponse.getPayload()).thenReturn(secretPayload);
            Mockito.when(secretPayload.getData()).thenReturn(ByteString.copyFrom("hello world".getBytes(StandardCharsets.UTF_8)));

            GCPSecretTransformer gcpSecretTransformer = new GCPSecretTransformer();
            var results = gcpSecretTransformer.process("db.connection", "gestalt-secret", "gcpSecret:gestalt-secret");

            Assertions.assertTrue(results.hasResults());
            Assertions.assertFalse(results.hasErrors());

            Assertions.assertEquals("hello world", results.results());
        }
    }

    @Test
    void processError() throws IOException {

        try (MockedStatic<SecretManagerServiceClient> secretClient = Mockito.mockStatic(SecretManagerServiceClient.class);
             MockedStatic<ServiceOptions> serviceOptions = Mockito.mockStatic(ServiceOptions.class)) {
            secretClient.when(SecretManagerServiceClient::create).thenReturn(secretManagerServiceClient);
            serviceOptions.when(ServiceOptions::getDefaultProjectId).thenReturn("testProject");

            SecretVersionName secretVersionName = SecretVersionName.of("testProject", "gestalt-secret", "latest");
            Mockito.when(secretManagerServiceClient.accessSecretVersion(secretVersionName))
                   .thenThrow(new ApiException("bad secret", new Throwable(), HttpJsonStatusCode.of(404), true));


            GCPSecretTransformer gcpSecretTransformer = new GCPSecretTransformer();
            var results = gcpSecretTransformer.process("db.connection", "gestalt-secret", "gcpSecret:gestalt-secret");

            Assertions.assertFalse(results.hasResults());
            Assertions.assertTrue(results.hasErrors());

            Assertions.assertEquals(1, results.getErrors().size());
            Assertions.assertEquals("Exception thrown while loading GCP secret: gestalt-secret, on path: db.connection in transformer:" +
                " gcpSecret, with message: bad secret", results.getErrors().get(0).description());
            Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        }
    }

    @Test
    void processErrorNullKey() {

        GCPSecretTransformer gcpSecretTransformer = new GCPSecretTransformer();
        var results = gcpSecretTransformer.process("db.connection", null, "gcpSecret:gestalt-secret");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("Invalid string: gcpSecret:gestalt-secret, on path: db.connection in transformer: gcpSecret",
            results.getErrors().get(0).description());
    }
}

