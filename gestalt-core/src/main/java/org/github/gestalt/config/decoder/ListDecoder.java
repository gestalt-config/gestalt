package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.PathUtil;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import static java.lang.System.Logger.Level.TRACE;

/**
 * Decode a list type.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class ListDecoder extends CollectionDecoder<List<?>> {
    private static final System.Logger logger = System.getLogger(ListDecoder.class.getName());

    Map<Class<?>, Supplier<List>> supplierMap = new HashMap<>();

    Class<?> sequencedCollection;

    @SuppressWarnings("JdkObsolete")
    public ListDecoder() {
        supplierMap.put(List.class, ArrayList::new);
        supplierMap.put(AbstractList.class, ArrayList::new);
        supplierMap.put(CopyOnWriteArrayList.class, CopyOnWriteArrayList::new);
        supplierMap.put(ArrayList.class, ArrayList::new);
        supplierMap.put(LinkedList.class, LinkedList::new);
        supplierMap.put(Stack.class, Stack::new);
        supplierMap.put(Vector.class, Vector::new);
        try {
            sequencedCollection = Class.forName("java.util.SequencedCollection");
            supplierMap.put(sequencedCollection, ArrayList::new);
        } catch (ClassNotFoundException e) {
            sequencedCollection = null;
            logger.log(TRACE, "Unable to find class java.util.SequencedCollection, SequencedCollectionDecoder disabled");
        }
    }

    @Override
    public String name() {
        return "List";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return List.class.isAssignableFrom(type.getRawType()) && type.hasParameter() ||
            (sequencedCollection != null && sequencedCollection.equals(type.getRawType())); // NOPMD
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected GResultOf<List<?>> arrayDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> klass,
                                             DecoderContext decoderContext) {
        List<ValidationError> errors = new ArrayList<>();
        Supplier<List> mapSupplier = supplierMap.get(klass.getRawType());

        if (mapSupplier == null) {
            logger.log(TRACE, "Unable to find supplier for " + klass.getRawType() + ", defaulting to ArrayList");
            mapSupplier = supplierMap.get(List.class);
        }

        List results = mapSupplier.get();

        for (int i = 0; i < node.size(); i++) {
            var valueOptional = node.getIndex(i);
            if (valueOptional.isPresent()) {
                ConfigNode currentNode = valueOptional.get();
                String nextPath = PathUtil.pathForIndex(decoderContext.getDefaultLexer(), path, i);
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
