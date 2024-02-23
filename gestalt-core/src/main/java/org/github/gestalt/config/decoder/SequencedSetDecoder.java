package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.PathUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Decode a Sequenced Set type.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class SequencedSetDecoder extends CollectionDecoder<Set<?>> {

    private static final System.Logger logger = System.getLogger(SequencedSetDecoder.class.getName());

    Class<?> sequencedSet;

    public SequencedSetDecoder() {
        try {
            sequencedSet = Class.forName("java.util.SequencedSet");
        } catch (ClassNotFoundException e) {
            sequencedSet = null;
            logger.log(System.Logger.Level.TRACE, "Unable to find class java.util.SequencedSet, SequencedSetDecoder disabled");
        }
    }

    @Override
    public String name() {
        return "SequencedSet";
    }

    @Override
    public Priority priority() {
        return Priority.HIGH;
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return sequencedSet != null && sequencedSet.isAssignableFrom(type.getRawType()) && type.hasParameter();
    }

    @Override
    protected GResultOf<Set<?>> arrayDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> klass, DecoderContext decoderContext) {
        List<ValidationError> errors = new ArrayList<>();
        Set<Object> results = new LinkedHashSet<>(node.size());

        for (int i = 0; i < node.size(); i++) {
            var valueOptional = node.getIndex(i);
            if (valueOptional.isPresent()) {
                ConfigNode currentNode = valueOptional.get();
                String nextPath = PathUtil.pathForIndex(path, i);
                GResultOf<?> resultOf = decoderContext.getDecoderService()
                    .decodeNode(nextPath, tags, currentNode, klass.getFirstParameterType(), decoderContext);

                errors.addAll(resultOf.getErrors());
                if (resultOf.hasResults()) {
                    results.add(resultOf.results());
                }

            } else {
                errors.add(new ValidationError.ArrayMissingIndex(i, path));
            }
        }


        return GResultOf.resultOf(results, errors);
    }
}
