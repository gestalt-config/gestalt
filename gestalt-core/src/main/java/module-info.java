/**
 * Module info definition for gestalt core
 */
module org.github.gestalt.core {
    uses org.github.gestalt.config.decoder.Decoder;
    uses org.github.gestalt.config.loader.ConfigLoader;
    uses org.github.gestalt.config.post.process.PostProcessor;
    uses org.github.gestalt.config.post.process.transform.Transformer;

    requires org.slf4j;

    exports org.github.gestalt.config;
    exports org.github.gestalt.config.annotations;
    exports org.github.gestalt.config.builder;
    exports org.github.gestalt.config.decoder;
    exports org.github.gestalt.config.entity;
    exports org.github.gestalt.config.exceptions;
    exports org.github.gestalt.config.lexer;
    exports org.github.gestalt.config.loader;
    exports org.github.gestalt.config.node;
    exports org.github.gestalt.config.parser;
    exports org.github.gestalt.config.post.process;
    exports org.github.gestalt.config.post.process.transform;
    exports org.github.gestalt.config.reflect;
    exports org.github.gestalt.config.reload;
    exports org.github.gestalt.config.source;
    exports org.github.gestalt.config.token;
    exports org.github.gestalt.config.utils;
    exports org.github.gestalt.config.tag;
}
