package org.github.gestalt.config.decoder;

import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.PathUtil;
import org.github.gestalt.config.utils.RecComponent;
import org.github.gestalt.config.utils.RecordUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Decoder support for Java Records.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
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

        boolean hasAllValues = true;
        List<ValidationError> errors = new ArrayList<>();
        Class<?> klass = type.getRawType();
        DecoderService decoderService = decoderContext.getDecoderService();

        final RecComponent[] recordComponents = RecordUtils.recordComponents(klass, Comparator.comparing(RecComponent::index));
        final Object[] values = new Object[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i++) {
            final RecComponent rc = recordComponents[i];

            String name = rc.name();
            // if we have an annotation, use that for the path instead of the name.
            Config configAnnotation = rc.getAccessor().getAnnotation(Config.class);
            if (configAnnotation != null && configAnnotation.path() != null && !configAnnotation.path().isEmpty()) {
                name = configAnnotation.path();
            }
            Type fieldClass = rc.typeGeneric();
            String nextPath = PathUtil.pathForKey(path, name);

            GResultOf<ConfigNode> configNode = decoderService.getNextNode(nextPath, name, node);
            var typeCapture = TypeCapture.of(fieldClass);

            errors.addAll(configNode.getErrors());
            if (!configNode.hasResults()) {
                // if we have no value, check the config annotation for a default.
                if (configAnnotation != null && configAnnotation.defaultVal() != null &&
                    !configAnnotation.defaultVal().isEmpty()) {
                    GResultOf<?> defaultGResultOf = decoderService.decodeNode(nextPath, tags, new LeafNode(configAnnotation.defaultVal()),
                        typeCapture, decoderContext);

                    errors.addAll(defaultGResultOf.getErrors());
                    if (defaultGResultOf.hasResults()) {
                        values[i] = defaultGResultOf.results();
                    } else {
                        hasAllValues = false;
                    }
                } else {
                    // when we have no result for the field and no annotation default
                    // try and decode the value anyway, in case its supports a nullable type, such as optional.
                    GResultOf<?> decodedResults =
                        decoderService.decodeNode(nextPath, tags, configNode.results(), typeCapture, decoderContext);
                    if (decodedResults.hasResults()) {
                        values[i] = decodedResults.results();
                    } else {
                        hasAllValues = false;
                    }
                }
            } else {
                GResultOf<?> fieldGResultOf =
                    decoderService.decodeNode(nextPath, tags, configNode.results(), typeCapture, decoderContext);

                errors.addAll(fieldGResultOf.getErrors());
                if (fieldGResultOf.hasResults()) {
                    values[i] = fieldGResultOf.results();
                } else {
                    hasAllValues = false;
                    errors.add(new ValidationError.NoResultsFoundForNode(nextPath, fieldClass.getClass(), "record decoding"));
                }
            }
        }

        if (!hasAllValues) {
            return GResultOf.errors(errors);
        }

        return GResultOf.resultOf(RecordUtils.invokeCanonicalConstructor(klass, recordComponents, values), errors);
    }
}
