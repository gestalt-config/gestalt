package org.github.gestalt.config.decoder;

import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationError.OptionalMissingValueDecoding;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.PathUtil;
import org.github.gestalt.config.utils.RecComponent;
import org.github.gestalt.config.utils.RecordUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Decoder support for Java Records.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class RecordDecoder implements Decoder<Object> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Record";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return RecordUtils.isRecord(type.getRawType());
    }

    @Override
    public GResultOf<Object> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {
        if (!(node instanceof MapNode)) {
            return GResultOf.errors(new ValidationError.DecodingExpectedLeafNodeType(path, node, name()));
        }

        List<ValidationError> errors = new ArrayList<>();
        Class<?> klass = type.getRawType();
        DecoderService decoderService = decoderContext.getDecoderService();

        final RecComponent[] recordComponents = RecordUtils.recordComponents(klass, Comparator.comparing(RecComponent::index));
        final Object[] values = new Object[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i++) {
            final RecComponent rc = recordComponents[i];
            boolean foundValue = false;

            String name = rc.name();

            Annotation[] annotations = rc.getDeclaredAnnotations();

            // if we have an annotation, use that for the path instead of the name.
            Config configAnnotation = rc.getAccessor().getAnnotation(Config.class);
            if (configAnnotation != null && configAnnotation.path() != null && !configAnnotation.path().isEmpty()) {
                name = configAnnotation.path();
            }
            Type fieldClass = rc.typeGeneric();
            String nextPath = PathUtil.pathForKey(decoderContext.getDefaultLexer(), path, name);

            GResultOf<ConfigNode> configNode = decoderService.getNextNode(nextPath, name, node);
            var typeCapture = TypeCapture.of(fieldClass);

            // Add any errors that are not missing value ones.
            errors.addAll(configNode.getErrorsNotLevel(ValidationLevel.MISSING_VALUE));
            if (configNode.hasResults()) {
                GResultOf<?> fieldGResultOf = decoderService.decodeNode(nextPath, tags, configNode.results(), typeCapture, decoderContext);

                errors.addAll(fieldGResultOf.getErrors());
                if (fieldGResultOf.hasResults()) {
                    foundValue = true;
                    values[i] = fieldGResultOf.results();
                }
            } else {
                // if we have no value, check the config annotation for a default.
                if (configAnnotation != null && configAnnotation.defaultVal() != null && !configAnnotation.defaultVal().isEmpty()) {
                    GResultOf<?> defaultGResultOf =
                        decoderService.decodeNode(nextPath, tags, new LeafNode(configAnnotation.defaultVal()), typeCapture, decoderContext);

                    errors.addAll(defaultGResultOf.getErrors());
                    if (defaultGResultOf.hasResults()) {
                        foundValue = true;
                        errors.add(new OptionalMissingValueDecoding(nextPath, node, name(), klass.getSimpleName(), decoderContext));
                        values[i] = defaultGResultOf.results();
                    }
                } else {
                    // when we have no result for the field and no annotation default
                    // try and decode the value anyway, in case its supports a nullable type, such as optional.
                    GResultOf<?> decodedResults =
                        decoderService.decodeNode(nextPath, tags, configNode.results(), typeCapture, decoderContext);
                    if (decodedResults.hasResults()) {
                        //only add the errors if we actually found a result, otherwise we dont care.
                        errors.addAll(decodedResults.getErrorsNotLevel(ValidationLevel.MISSING_OPTIONAL_VALUE));
                        errors.add(new OptionalMissingValueDecoding(nextPath, node, name(), klass.getSimpleName(), decoderContext));
                        foundValue = true;
                        values[i] = decodedResults.results();
                    }
                }
            }

            if (!foundValue) {
                // check the record Components to see if it is annotated with nullable.
                boolean isNullable = isNullableAnnotation(annotations);

                values[i] = null;
                if (!isNullable) {
                    errors.add(new ValidationError.NoResultsFoundForNode(nextPath, klass.getSimpleName(), "record decoding"));
                } else {
                    errors.add(new OptionalMissingValueDecoding(nextPath, node, name(), klass.getSimpleName(), decoderContext));
                }
            }
        }

        return GResultOf.resultOf(RecordUtils.invokeCanonicalConstructor(klass, recordComponents, values), errors);
    }

    private static boolean isNullableAnnotation(Annotation[] fieldAnnotations) {
        return Arrays.stream(fieldAnnotations)
            .anyMatch(it -> it.annotationType().getName().toLowerCase(Locale.ROOT).contains("nullable"));
    }
}
