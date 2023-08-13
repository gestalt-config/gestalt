/*
 * Module info definition for gestalt yaml integration
 */
module org.github.gestalt.vault {
    requires org.github.gestalt.core;
    requires vault.java.driver;

    exports org.github.gestalt.config.vault;
}
