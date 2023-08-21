/**
 * Module info definition for gestalt yaml integration
 */
module org.github.gestalt.config.integration {
    requires org.github.gestalt.core;
    requires org.github.gestalt.guice;
    requires org.github.gestalt.hocon;
    requires org.github.gestalt.json;
    requires org.github.gestalt.yaml;
    requires org.github.gestalt.config.kotlin;

    requires com.google.guice;
    requires org.junit.jupiter.api;
    requires kotlin.stdlib;
    requires software.amazon.awssdk.http;
    requires software.amazon.awssdk.services.s3;
    requires org.github.gestalt.git;
    requires jakarta.inject;
    requires org.github.gestalt.google;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.http.urlconnection;
    requires org.github.gestalt.aws;
    requires org.github.gestalt.vault;

    exports org.github.gestalt.config.integration;

    //you need to export any data classes you want to use the Object decoder on (since it uses reflection).
    opens org.github.gestalt.config.integration to org.github.gestalt.core, org.github.gestalt.guice, org.github.gestalt.config.kotlin;
}
