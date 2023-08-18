/*
 * Module info definition for gestalt hocon integration
 */
@SuppressWarnings({ "requires-automatic", "requires-transitive-automatic" })
module org.github.gestalt.guice {
    requires org.github.gestalt.core;
    requires transitive com.google.guice;

    exports org.github.gestalt.config.guice;
}
