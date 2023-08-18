/*
 *  Module info definition for gestalt git integration
 */
@SuppressWarnings({ "requires-automatic", "requires-transitive-automatic" })
module org.github.gestalt.git {
    requires org.github.gestalt.core;
    requires transitive org.eclipse.jgit;

    exports org.github.gestalt.config.git;
}
