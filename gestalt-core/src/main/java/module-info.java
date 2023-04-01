/**
 * Module info definition for gestalt core
 */
module org.github.gestalt.core {
    uses org.github.gestalt.config.decoder.Decoder;
    uses org.github.gestalt.config.loader.ConfigLoader;
    uses org.github.gestalt.config.path.mapper.PathMapper;
    uses org.github.gestalt.config.post.process.PostProcessor;
    uses org.github.gestalt.config.post.process.transform.Transformer;

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
    exports org.github.gestalt.config.path.mapper;
    exports org.github.gestalt.config.post.process;
    exports org.github.gestalt.config.post.process.transform;
    exports org.github.gestalt.config.reflect;
    exports org.github.gestalt.config.reload;
    exports org.github.gestalt.config.source;
    exports org.github.gestalt.config.tag;
    exports org.github.gestalt.config.token;
    exports org.github.gestalt.config.utils;

    provides org.github.gestalt.config.decoder.Decoder with
        org.github.gestalt.config.decoder.ArrayDecoder,
        org.github.gestalt.config.decoder.BigDecimalDecoder,
        org.github.gestalt.config.decoder.BigIntegerDecoder,
        org.github.gestalt.config.decoder.BooleanDecoder,
        org.github.gestalt.config.decoder.ByteDecoder,
        org.github.gestalt.config.decoder.CharDecoder,
        org.github.gestalt.config.decoder.DateDecoder,
        org.github.gestalt.config.decoder.DoubleDecoder,
        org.github.gestalt.config.decoder.DurationDecoder,
        org.github.gestalt.config.decoder.EnumDecoder,
        org.github.gestalt.config.decoder.FileDecoder,
        org.github.gestalt.config.decoder.FloatDecoder,
        org.github.gestalt.config.decoder.InstantDecoder,
        org.github.gestalt.config.decoder.IntegerDecoder,
        org.github.gestalt.config.decoder.ListDecoder,
        org.github.gestalt.config.decoder.LocalDateDecoder,
        org.github.gestalt.config.decoder.LocalDateTimeDecoder,
        org.github.gestalt.config.decoder.LongDecoder,
        org.github.gestalt.config.decoder.MapDecoder,
        org.github.gestalt.config.decoder.ObjectDecoder,
        org.github.gestalt.config.decoder.OptionalDecoder,
        org.github.gestalt.config.decoder.OptionalDoubleDecoder,
        org.github.gestalt.config.decoder.OptionalIntDecoder,
        org.github.gestalt.config.decoder.OptionalLongDecoder,
        org.github.gestalt.config.decoder.PathDecoder,
        org.github.gestalt.config.decoder.PatternDecoder,
        org.github.gestalt.config.decoder.ProxyDecoder,
        org.github.gestalt.config.decoder.RecordDecoder,
        org.github.gestalt.config.decoder.SetDecoder,
        org.github.gestalt.config.decoder.ShortDecoder,
        org.github.gestalt.config.decoder.StringDecoder,
        org.github.gestalt.config.decoder.UUIDDecoder;

    provides org.github.gestalt.config.loader.ConfigLoader with
        org.github.gestalt.config.loader.EnvironmentVarsLoader,
        org.github.gestalt.config.loader.MapConfigLoader,
        org.github.gestalt.config.loader.PropertyLoader;

    provides org.github.gestalt.config.path.mapper.PathMapper with
        org.github.gestalt.config.path.mapper.StandardPathMapper,
        org.github.gestalt.config.path.mapper.CamelCasePathMapper;

    provides org.github.gestalt.config.post.process.PostProcessor with
        org.github.gestalt.config.post.process.transform.TransformerPostProcessor;

    provides org.github.gestalt.config.post.process.transform.Transformer with
        org.github.gestalt.config.post.process.transform.EnvironmentVariablesTransformer,
        org.github.gestalt.config.post.process.transform.EnvironmentVariablesTransformerOld,
        org.github.gestalt.config.post.process.transform.SystemPropertiesTransformer,
        org.github.gestalt.config.post.process.transform.NodeTransformer,
        org.github.gestalt.config.post.process.transform.RandomTransformer;
}
