/*
 * Module info definition for gestalt json integration
 */
module org.github.gestalt.json {
    requires org.github.gestalt.core;
    requires transitive tools.jackson.databind;

    exports org.github.gestalt.config.json;

    provides org.github.gestalt.config.loader.ConfigLoader with
        org.github.gestalt.config.json.JsonLoader;
}
