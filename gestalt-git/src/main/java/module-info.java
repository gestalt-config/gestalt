
/*
 *  Module info definition for gestalt git integration
 */
@SuppressWarnings({ "requires-automatic", "requires-transitive-automatic" })
module org.github.gestalt.git {
    requires org.github.gestalt.core;
    requires transitive org.eclipse.jgit;

    exports org.github.gestalt.config.git;
    exports org.github.gestalt.config.git.builder;
    exports org.github.gestalt.config.git.config;
    exports org.github.gestalt.config.git.node.factory;

    provides org.github.gestalt.config.node.factory.ConfigNodeFactory with
        org.github.gestalt.config.git.node.factory.GitConfigNodeFactory;
}
