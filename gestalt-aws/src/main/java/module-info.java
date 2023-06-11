// Module info definition for gestalt s3 integration
module org.github.gestalt.aws {
    requires org.github.gestalt.core;
    requires software.amazon.awssdk.services.s3;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.services.secretsmanager;
    requires software.amazon.awssdk.http.urlconnection;
    requires com.fasterxml.jackson.databind;

    exports org.github.gestalt.config.aws.builder;
    exports org.github.gestalt.config.aws.config;
    exports org.github.gestalt.config.aws.errors;
    exports org.github.gestalt.config.aws.s3;
    exports org.github.gestalt.config.aws.transformer;
}

