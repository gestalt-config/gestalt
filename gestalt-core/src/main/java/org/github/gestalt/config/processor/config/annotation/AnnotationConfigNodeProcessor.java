package org.github.gestalt.config.processor.config.annotation;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.metadata.MetaDataValue;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.processor.config.ConfigNodeProcessor;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.utils.GResultOf;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Processor that scans nodes to find annotations in the format @{myAnnotation} then adds the annotations as metadata to the config node.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
@ConfigPriority(100)
public class AnnotationConfigNodeProcessor implements ConfigNodeProcessor {

    public static final String DEFAULT_ANNOTATION_REGEX =
        "^(?<annotation>\\w+)(:(?<parameter>.+?))?$";
    private final Map<String, AnnotationMetadataTransform> annotationMetadataTransforms = new HashMap<>();
    private String openingToken = "@{";
    private String closingToken = "}";
    private int openingTokenSize = openingToken.length();
    private int closingTokenSize = closingToken.length();
    private Pattern pattern = Pattern.compile(DEFAULT_ANNOTATION_REGEX);

    public AnnotationConfigNodeProcessor() {
        ServiceLoader<AnnotationMetadataTransform> loader = ServiceLoader.load(AnnotationMetadataTransform.class);
        loader.forEach(it -> annotationMetadataTransforms.put(it.name(), it));
    }

    public AnnotationConfigNodeProcessor(List<AnnotationMetadataTransform> annotationMetadataList) {
        annotationMetadataTransforms.putAll(annotationMetadataList.stream()
            .collect(Collectors.toMap(AnnotationMetadataTransform::name, Function.identity())));
    }

    @Override
    public void applyConfig(ConfigNodeProcessorConfig config) {
        this.openingToken = config.getConfig().getAnnotationOpeningToken();
        this.openingTokenSize = openingToken.length();

        this.closingToken = config.getConfig().getAnnotationClosingToken();
        this.closingTokenSize = closingToken.length();

        this.pattern = Pattern.compile(config.getConfig().getAnnotationRegex());
    }

    @Override
    public GResultOf<ConfigNode> process(String path, ConfigNode currentNode) {
        var valueOptional = currentNode.getValue();
        if (!(currentNode instanceof LeafNode) || valueOptional.isEmpty() || valueOptional.get().isEmpty()) {
            return GResultOf.result(currentNode);
        }

        // check to see if there is an annotation.
        String leafValue = valueOptional.get();
        int annotationLocation = leafValue.indexOf(openingToken);
        if (annotationLocation < 0) {
            // if no annotation, return the current string.
            return GResultOf.result(currentNode);
        }

        List<ValidationError> errors = new ArrayList<>();

        // if there is at least one annotation
        Map<String, List<MetaDataValue<?>>> metadataMap = new HashMap<>();

        while (annotationLocation >= 0) {
            boolean foundAnnotation = false;
            int annotationClosing = leafValue.indexOf(closingToken, annotationLocation);

            // if there is a full annotation
            if (annotationClosing > 0) {
                String annotation = leafValue.substring(annotationLocation + openingTokenSize, annotationClosing);

                Matcher matcher = pattern.matcher(annotation);

                if (!matcher.find()) {
                    errors.add(new ValidationError.FailedToExtractAnnotation(annotation, path));
                } else {

                    String annotationName = matcher.group("annotation");
                    String parameter = matcher.group("parameter");

                    if (annotationMetadataTransforms.containsKey(annotationName.toLowerCase(Locale.ROOT))) {
                        var metadata = annotationMetadataTransforms
                            .get(annotationName.toLowerCase(Locale.ROOT))
                            .annotationTransform(annotationName, parameter);

                        errors.addAll(metadata.getErrors());
                        metadataMap.putAll(metadata.results());
                        // remove the found annotation
                        leafValue = leafValue.substring(0, annotationLocation) + leafValue.substring(annotationClosing + closingTokenSize);
                        foundAnnotation = true;
                    } else {
                        errors.add(new ValidationError.UnknownAnnotation(path, annotation));
                    }
                }
                if (!foundAnnotation) {
                    annotationLocation = annotationLocation + openingTokenSize;
                }
                // Scan for the next annotation starting from the last one.
                annotationLocation = leafValue.indexOf(openingToken, annotationLocation);
            } else {
                errors.add(new ValidationError.NoAnnotationClosingToken(path, leafValue));
                break;
            }
        }

        return GResultOf.resultOf(new LeafNode(leafValue, metadataMap), errors);
    }
}
