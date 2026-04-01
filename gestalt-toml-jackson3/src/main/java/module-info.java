/*
 * Module info definition for gestalt yaml integration
 */
module org.github.gestalt.toml {
    requires org.github.gestalt.core;
    requires transitive tools.jackson.databind;
    requires transitive tools.jackson.dataformat.toml;

    exports org.github.gestalt.config.toml;

    provides org.github.gestalt.config.loader.ConfigLoader with
        org.github.gestalt.config.toml.TomlLoader;
}
