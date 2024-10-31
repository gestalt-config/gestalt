package org.github.gestalt.config.processor.config.annotation;

import org.github.gestalt.config.metadata.MetaDataValue;
import org.github.gestalt.config.utils.GResultOf;

import java.util.List;
import java.util.Map;

public interface AnnotationMetadataTransform {

    /**
     * the name that will match the ${annotation:parameter} the transform is selected that matches the same name.
     *
     * @return the name of the transform
     */
    String name();

    /**
     * Takes in the name of the annotation along with any parameters then returns a MetaDataValue.
     *
     * @param name      name of the annotation
     * @param parameter parameters for the annotation
     * @return a annotation converted into a MetaDataValue
     */
    GResultOf<Map<String, List<MetaDataValue<?>>>> annotationTransform(String name, String parameter);
}
