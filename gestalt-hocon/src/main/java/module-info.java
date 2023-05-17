/*
 * Module info definition for gestalt hocon integration
 */
module org.github.gestalt.hocon {
    requires org.github.gestalt.core;
    requires typesafe.config;

    exports org.github.gestalt.config.hocon;

    provides org.github.gestalt.config.loader.ConfigLoader with
        org.github.gestalt.config.hocon.HoconLoader;
}

