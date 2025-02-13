package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.PathUtil;

import java.util.*;
import java.util.function.Supplier;

import static java.lang.System.Logger.Level.TRACE;

/**
 * Decode a Set type.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class SetDecoder extends CollectionDecoder<Set<?>> {
    private static final System.Logger logger = System.getLogger(SetDecoder.class.getName());

    Map<Class<?>, Supplier<Set>> supplierMap = new HashMap<>();
    Class<?> sequencedSet;

    public SetDecoder() {
        supplierMap.put(Set.class, HashSet::new);
        supplierMap.put(HashSet.class, HashSet::new);
        supplierMap.put(TreeSet.class, TreeSet::new);
        supplierMap.put(LinkedHashSet.class, LinkedHashSet::new);
        try {
            sequencedSet = Class.forName("java.util.SequencedSet");
            supplierMap.put(sequencedSet, LinkedHashSet::new);
        } catch (ClassNotFoundException e) {
            sequencedSet = null;
            logger.log(TRACE, "Unable to find class java.util.SequencedSet, SequencedSetDecoder disabled");
        }
    }

    @Override
    public String name() {
        return "Set";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return Set.class.isAssignableFrom(type.getRawType()) && type.hasParameter();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected GResultOf<Set<?>> arrayDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> klass, DecoderContext decoderContext) {
        List<ValidationError> errors = new ArrayList<>();

        Supplier<Set> mapSupplier = supplierMap.get(klass.getRawType());
        if (mapSupplier == null) {
            logger.log(TRACE, "Unable to find supplier for " + klass.getRawType() + ", defaulting to HashSet");
            mapSupplier = supplierMap.get(Set.class);
        }

        Set<Object> results = mapSupplier.get();
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
                errors.add(new ValidationError.ArrayMissingIndex(i, path));
            }
        }


        return GResultOf.resultOf(results, errors);
    }
}
