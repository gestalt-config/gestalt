// Module info definition for gestalt azure integration
module org.github.gestalt.azure {
    requires org.github.gestalt.core;
    requires transitive com.azure.core;
    requires transitive com.azure.identity;
    requires transitive com.azure.security.keyvault.secrets;
    requires transitive com.azure.storage.blob;
    requires transitive com.azure.storage.common;

    exports org.github.gestalt.config.azure.blob;
    exports org.github.gestalt.config.azure.config;
    exports org.github.gestalt.config.azure.errors;
    exports org.github.gestalt.config.azure.transformer;

    provides org.github.gestalt.config.processor.config.transform.Transformer with
        org.github.gestalt.config.azure.transformer.AzureSecretTransformer;
}
