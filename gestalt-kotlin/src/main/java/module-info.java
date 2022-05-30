/**
 * Module info definition for gestalt kotlin integration
 */
module org.github.gestalt.kotlin {
    requires org.github.gestalt.core;
    requires kotlin.reflect;

    exports org.github.gestalt.config.kotlin;
    exports org.github.gestalt.config.kotlin.decoder;
    exports org.github.gestalt.config.kotlin.entity;
    exports org.github.gestalt.config.kotlin.reflect;
}
