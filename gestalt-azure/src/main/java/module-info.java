// Module info definition for gestalt s3 integration
module org.github.gestalt.azure {
    requires org.github.gestalt.core;
    requires com.azure.storage.blob;
    requires com.azure.security.keyvault.secrets;
    requires com.azure.identity;

    exports org.github.gestalt.config.azure.blob;
    exports org.github.gestalt.config.azure.config;
    exports org.github.gestalt.config.azure.errors;
    exports org.github.gestalt.config.azure.transformer;

    provides org.github.gestalt.config.processor.config.transform.Transformer with
        org.github.gestalt.config.azure.transformer.AzureSecretTransformer;
}

