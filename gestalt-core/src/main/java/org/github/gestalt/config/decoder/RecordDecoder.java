package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.utils.PathUtil;
import org.github.gestalt.config.utils.RecComponent;
import org.github.gestalt.config.utils.RecordUtils;
import org.github.gestalt.config.utils.ValidateOf;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Decoder support for Java Records.
 *
 * @author Colin Redmond
 */
public class RecordDecoder implements Decoder<Object> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Record";
    }

    @Override
    public boolean matches(TypeCapture<?> klass) {
        return RecordUtils.isRecord(klass.getRawType());
    }

    @Override
    public ValidateOf<Object> decode(String path, ConfigNode node, TypeCapture<?> type, DecoderService decoderService) {
        if (!(node instanceof MapNode)) {
            return ValidateOf.inValid(new ValidationError.DecodingExpectedLeafNodeType(path, node, name()));
        }

        List<ValidationError> errors = new ArrayList<>();
        Class<?> klass = type.getRawType();

        final RecComponent[] recordComponents = RecordUtils.recordComponents(klass, Comparator.comparing(RecComponent::index));
        final Object[] values = new Object[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i++) {
            final RecComponent rc = recordComponents[i];

            String name = rc.name();
            Type fieldClass = rc.typeGeneric();
            String nextPath = PathUtil.pathForKey(path, name);

            ValidateOf<ConfigNode> configNode = decoderService.getNextNode(nextPath, name, node);

            errors.addAll(configNode.getErrors(ValidationLevel.WARN));
            if (configNode.hasErrors(ValidationLevel.ERROR)) {
                errors.addAll(configNode.getErrors(ValidationLevel.ERROR));
            } else if (!configNode.hasResults()) {
                errors.add(new ValidationError.NoResultsFoundForNode(nextPath, fieldClass.getClass(), "record decoding"));
            } else {
                ValidateOf<?> fieldValidateOf = decoderService.decodeNode(nextPath, configNode.results(),
                    TypeCapture.of(fieldClass));

                if (fieldValidateOf.hasErrors()) {
                    errors.addAll(fieldValidateOf.getErrors());
                }

                if (fieldValidateOf.hasResults()) {
                    values[i] = fieldValidateOf.results();
                } else {
                    errors.add(new ValidationError.NoResultsFoundForNode(nextPath, fieldClass.getClass(), "record decoding"));
                }
            }
        }

        if (!errors.isEmpty()) {
            return ValidateOf.inValid(errors);
        }

        return ValidateOf.valid(RecordUtils.invokeCanonicalConstructor(klass, recordComponents, values));
    }
}
