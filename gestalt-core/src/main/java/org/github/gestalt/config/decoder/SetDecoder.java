package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.PathUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Decode a Set type.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class SetDecoder extends CollectionDecoder<Set<?>> {

    @Override
    public String name() {
        return "Set";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Set.class.isAssignableFrom(type.getRawType()) && type.hasParameter();
    }

    @Override
    protected GResultOf<Set<?>> arrayDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> klass, DecoderContext decoderContext) {
        List<ValidationError> errors = new ArrayList<>();
        Set<Object> results = new HashSet<>(node.size());

        for (int i = 0; i < node.size(); i++) {
            if (node.getIndex(i).isPresent()) {
                ConfigNode currentNode = node.getIndex(i).get();
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
