/*
 * Module info definition for gestalt yaml integration
 */
module org.github.gestalt.vault {
    requires org.github.gestalt.core;
    requires transitive vault.java.driver;

    exports org.github.gestalt.config.vault;
    exports org.github.gestalt.config.vault.config;
    exports org.github.gestalt.config.vault.errors;
}
