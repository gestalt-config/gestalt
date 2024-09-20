// Module info definition for gestalt aws integration
@SuppressWarnings({ "requires-automatic", "requires-transitive-automatic" })
module org.github.gestalt.aws {
    requires org.github.gestalt.core;
    requires transitive software.amazon.awssdk.services.s3;
    requires transitive software.amazon.awssdk.core;
    requires transitive software.amazon.awssdk.auth;
    requires transitive software.amazon.awssdk.regions;
    requires transitive software.amazon.awssdk.services.secretsmanager;
    requires transitive software.amazon.awssdk.http.urlconnection;
    requires transitive com.fasterxml.jackson.databind;

    exports org.github.gestalt.config.aws.config;
    exports org.github.gestalt.config.aws.errors;
    exports org.github.gestalt.config.aws.s3;
    exports org.github.gestalt.config.aws.transformer;

    provides org.github.gestalt.config.processor.config.transform.Transformer with
        org.github.gestalt.config.aws.transformer.AWSSecretTransformer;

    provides org.github.gestalt.config.node.factory.ConfigNodeFactory with
        org.github.gestalt.config.aws.node.factory.S3ConfigNodeFactory;
}

