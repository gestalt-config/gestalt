/*
 * Module info definition for gestalt yaml integration
 */
module org.github.gestalt.yaml {
    requires org.github.gestalt.core;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.dataformat.yaml;

    exports org.github.gestalt.config.yaml;

    provides org.github.gestalt.config.loader.ConfigLoader with
        org.github.gestalt.config.yaml.YamlLoader;
}
