package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.PathUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Decode a Sequenced list type.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class SequencedCollectionDecoder extends CollectionDecoder<List<?>> {

    private static final System.Logger logger = System.getLogger(SequencedCollectionDecoder.class.getName());

    Class<?> sequencedCollection;

    public SequencedCollectionDecoder() {
        try {
            sequencedCollection = Class.forName("java.util.SequencedCollection");
        } catch (ClassNotFoundException e) {
            sequencedCollection = null;
            logger.log(System.Logger.Level.TRACE, "Unable to find class java.util.SequencedCollection, " +
                "SequencedCollectionDecoder disabled");
        }
    }

    @Override
    public String name() {
        return "SequencedCollection";
    }

    @Override
    public Priority priority() {
        return Priority.HIGH;
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return sequencedCollection != null && sequencedCollection.equals(type.getRawType()) && type.hasParameter();
    }

    @Override
    protected GResultOf<List<?>> arrayDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> klass,
                                             DecoderContext decoderContext) {
        List<ValidationError> errors = new ArrayList<>();
        List<Object> results = new ArrayList<>(node.size());

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
                errors.add(new ValidationError.ArrayMissingIndex(i));
                results.add(null);
            }
        }


        return GResultOf.resultOf(results, errors);
    }
}
