/**
 * Module info definition for gestalt kotlin integration
 */
module org.github.gestalt.config.kotlin {
    requires org.github.gestalt.core;
    requires transitive kotlin.reflect;
    requires java.base;

    exports org.github.gestalt.config.kotlin;
    exports org.github.gestalt.config.kotlin.decoder;
    exports org.github.gestalt.config.kotlin.entity;
    exports org.github.gestalt.config.kotlin.reflect;

    provides org.github.gestalt.config.decoder.Decoder with
        org.github.gestalt.config.kotlin.decoder.BooleanDecoder,
        org.github.gestalt.config.kotlin.decoder.ByteDecoder,
        org.github.gestalt.config.kotlin.decoder.CharDecoder,
        org.github.gestalt.config.kotlin.decoder.DataClassDecoder,
        org.github.gestalt.config.kotlin.decoder.DoubleDecoder,
        org.github.gestalt.config.kotlin.decoder.FloatDecoder,
        org.github.gestalt.config.kotlin.decoder.IntegerDecoder,
        org.github.gestalt.config.kotlin.decoder.LongDecoder,
        org.github.gestalt.config.kotlin.decoder.ShortDecoder,
        org.github.gestalt.config.kotlin.decoder.StringDecoder;
}
