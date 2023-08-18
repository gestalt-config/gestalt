/*
 * Module info definition for gestalt hocon integration
 */
@SuppressWarnings({ "requires-automatic", "requires-transitive-automatic" })
module org.github.gestalt.hocon {
    requires org.github.gestalt.core;
    requires transitive typesafe.config;

    exports org.github.gestalt.config.hocon;

    provides org.github.gestalt.config.loader.ConfigLoader with
        org.github.gestalt.config.hocon.HoconLoader;
}

