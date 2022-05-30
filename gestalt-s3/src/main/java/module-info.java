// Module info definition for gestalt s3 integration
module org.github.gestalt.s3 {
    requires org.github.gestalt.core;
    requires software.amazon.awssdk.services.s3;
    requires software.amazon.awssdk.core;

    exports org.github.gestalt.config.aws.s3;
}
