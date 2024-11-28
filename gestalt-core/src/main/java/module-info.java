/*
 * Module info definition for gestalt core
 */
module org.github.gestalt.core {
    uses org.github.gestalt.config.decoder.Decoder;
    uses org.github.gestalt.config.loader.ConfigLoader;
    uses org.github.gestalt.config.path.mapper.PathMapper;
    uses org.github.gestalt.config.processor.config.ConfigNodeProcessor;
    uses org.github.gestalt.config.processor.config.RunTimeConfigNodeProcessor;
    uses org.github.gestalt.config.processor.config.transform.Transformer;
    uses org.github.gestalt.config.processor.result.ResultProcessor;
    uses org.github.gestalt.config.observations.ObservationRecorder;
    uses org.github.gestalt.config.processor.result.validation.ConfigValidator;
    uses org.github.gestalt.config.node.factory.ConfigNodeFactory;
    uses org.github.gestalt.config.processor.config.annotation.AnnotationMetadataTransform;

    exports org.github.gestalt.config;
    exports org.github.gestalt.config.annotations;
    exports org.github.gestalt.config.builder;
    exports org.github.gestalt.config.decoder;
    exports org.github.gestalt.config.entity;
    exports org.github.gestalt.config.exceptions;
    exports org.github.gestalt.config.lexer;
    exports org.github.gestalt.config.loader;
    exports org.github.gestalt.config.metadata;
    exports org.github.gestalt.config.node;
    exports org.github.gestalt.config.node.factory;
    exports org.github.gestalt.config.observations;
    exports org.github.gestalt.config.parser;
    exports org.github.gestalt.config.path.mapper;
    exports org.github.gestalt.config.processor.config;
    exports org.github.gestalt.config.processor.config.annotation;
    exports org.github.gestalt.config.processor.config.include;
    exports org.github.gestalt.config.processor.config.transform;
    exports org.github.gestalt.config.processor.config.transform.substitution;
    exports org.github.gestalt.config.processor.result;
    exports org.github.gestalt.config.processor.result.validation;
    exports org.github.gestalt.config.reflect;
    exports org.github.gestalt.config.reload;
    exports org.github.gestalt.config.secret.rules;
    exports org.github.gestalt.config.security.encrypted;
    exports org.github.gestalt.config.security.temporary;
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
        org.github.gestalt.config.path.mapper.DotNotationPathMapper,
        org.github.gestalt.config.path.mapper.KebabCasePathMapper,
        org.github.gestalt.config.path.mapper.StandardPathMapper,
        org.github.gestalt.config.path.mapper.SnakeCasePathMapper;

    provides org.github.gestalt.config.processor.config.ConfigNodeProcessor with
        org.github.gestalt.config.processor.config.transform.LoadtimeStringSubstitutionConfigNodeProcessor,
        org.github.gestalt.config.security.encrypted.EncryptedSecretConfigNodeProcessor,
        org.github.gestalt.config.security.temporary.TemporarySecretConfigNodeProcessor,
        org.github.gestalt.config.processor.config.include.IncludeConfigNodeProcessor,
        org.github.gestalt.config.processor.config.annotation.AnnotationConfigNodeProcessor,
        org.github.gestalt.config.processor.config.transform.RunTimeMetadataConfigNodeProcessor;

    provides org.github.gestalt.config.processor.config.RunTimeConfigNodeProcessor with
        org.github.gestalt.config.processor.config.transform.RunTimeStringSubstitutionConfigNodeProcessor;

    provides org.github.gestalt.config.processor.config.transform.Transformer with
        org.github.gestalt.config.processor.config.transform.Base64DecoderTransformer,
        org.github.gestalt.config.processor.config.transform.Base64EncoderTransformer,
        org.github.gestalt.config.processor.config.transform.ClasspathTransformer,
        org.github.gestalt.config.processor.config.transform.Distribution100Transformer,
        org.github.gestalt.config.processor.config.transform.EnvironmentVariablesTransformer,
        org.github.gestalt.config.processor.config.transform.EnvironmentVariablesTransformerOld,
        org.github.gestalt.config.processor.config.transform.FileTransformer,
        org.github.gestalt.config.processor.config.transform.SystemPropertiesTransformer,
        org.github.gestalt.config.processor.config.transform.NodeTransformer,
        org.github.gestalt.config.processor.config.transform.RandomTransformer,
        org.github.gestalt.config.processor.config.transform.URLDecoderTransformer,
        org.github.gestalt.config.processor.config.transform.URLEncoderTransformer;

    provides org.github.gestalt.config.node.factory.ConfigNodeFactory with
        org.github.gestalt.config.node.factory.ClassPathConfigNodeFactory,
        org.github.gestalt.config.node.factory.ConfigNodeImportFactory,
        org.github.gestalt.config.node.factory.EnvVarsConfigNodeFactory,
        org.github.gestalt.config.node.factory.FileConfigNodeFactory,
        org.github.gestalt.config.node.factory.KubernetesSecretConfigNodeFactory,
        org.github.gestalt.config.node.factory.SystemConfigNodeFactory,
        org.github.gestalt.config.node.factory.UrlConfigNodeFactory;

    provides org.github.gestalt.config.processor.config.annotation.AnnotationMetadataTransform with
        org.github.gestalt.config.processor.config.annotation.EncryptionAnnotationMetadataTransform,
        org.github.gestalt.config.processor.config.annotation.TemporaryAnnotationMetadataTransform,
        org.github.gestalt.config.processor.config.annotation.NoCacheAnnotationMetadataTransform,
        org.github.gestalt.config.processor.config.annotation.SecretAnnotationMetadataTransform;
}


