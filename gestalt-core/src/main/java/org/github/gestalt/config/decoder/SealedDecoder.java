package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.SealedClassUtil;

import java.lang.reflect.Field;

import static java.lang.Math.abs;
import static org.github.gestalt.config.utils.SealedClassUtil.getPermittedSubclasses;

/**
 * Decode a Short.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class SealedDecoder implements Decoder<Object> {

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public String name() {
        return "Sealed";
    }

    @Override
    public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
        return SealedClassUtil.isSealed(type.getRawType());
    }

    @SuppressWarnings("unchecked")
    @Override
    public GResultOf<Object> decode(String path, Tags tags, ConfigNode node, TypeCapture<?> type, DecoderContext decoderContext) {

        Class<?>[] candidates = getPermittedSubclasses(type.getRawType());
        if (candidates.length == 0) {
            return GResultOf.errors(new ValidationError.NoPermittedClassesInSealedClass(type.getName(), path));
        }

        // Score each candidate by the number of errors while decoding and return the best one.
        GResultOf<Object> best = null;
        int bestScore = Integer.MAX_VALUE;
        for (Class<?> cand : candidates) {
            var candidateDecoded = decoderContext.getDecoderService().decodeNode(path, tags, node, TypeCapture.of(cand), decoderContext);

            if (!candidateDecoded.hasResults() && bestScore < Integer.MAX_VALUE) {
                best = (GResultOf<Object>) candidateDecoded;
                continue;
            } else  if (!candidateDecoded.hasResults()) {
                continue;
            }

            int score = candidateDecoded.getErrors().stream().map(it -> {
                int points;
                switch (it.level()) {
                    case ERROR:
                        points = 5;
                        break;
                    case MISSING_VALUE:
                        points = 4;
                        break;
                    case MISSING_OPTIONAL_VALUE:
                        points = 3;
                        break;
                    case WARN:
                        points = 2;
                        break;
                    case DEBUG:
                        points = 1;
                        break;
                    default:
                        points = 0;
                        break;
                }

                return points;
            }).reduce(0, Integer::sum);

            // add to the score the difference in number of fields between the node and the candidate class
            int fieldCount = 0;
            Class<?> currentClass = candidateDecoded.results().getClass();
            while (currentClass != null) {
                final Field[] fields = currentClass.getDeclaredFields();
                fieldCount += fields.length;
                currentClass = currentClass.getSuperclass();
            }
            score = score + abs(node.size() - fieldCount) * 2;

            if (score < bestScore) {
                best = (GResultOf<Object>) candidateDecoded;
                bestScore = score;
            }
        }

        return best;
    }
}
