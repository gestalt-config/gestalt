package org.github.gestalt.config.processor.config.transform;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.metadata.IsNoCacheMetadata;
import org.github.gestalt.config.metadata.IsRunTimeStringSubstitutionMetadata;
import org.github.gestalt.config.metadata.MetaDataValue;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.processor.config.ConfigNodeProcessor;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.utils.GResultOf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processor that scans nodes to find any run time tokens and marks those nodes with IsRunTimeStringSubstitutionMetadata.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
@ConfigPriority(100)
public final class RunTimeMetadataConfigNodeProcessor implements ConfigNodeProcessor {

    private String openingToken = "#{";
    private String closingToken = "}";

    public RunTimeMetadataConfigNodeProcessor() {
    }


    @Override
    public void applyConfig(ConfigNodeProcessorConfig config) {
        this.openingToken = config.getConfig().getRunTimeSubstitutionOpeningToken();
        this.closingToken = config.getConfig().getRunTimeSubstitutionClosingToken();
    }

    @Override
    public GResultOf<ConfigNode> process(String path, ConfigNode currentNode) {
        var valueOptional = currentNode.getValue();
        if (!(currentNode instanceof LeafNode) || valueOptional.isEmpty() || valueOptional.get().isEmpty()) {
            return GResultOf.result(currentNode);
        }

        // check to see if there is a run time string sub opening token.
        String leafValue = valueOptional.get();
        int runTimeStringSubLocation = leafValue.indexOf(openingToken);
        if (runTimeStringSubLocation < 0) {
            // if no run time string sub token, return the current string.
            return GResultOf.result(currentNode);
        }

        int runTimeStringSubClosing = leafValue.indexOf(closingToken, runTimeStringSubLocation);

        // if there is at least one run time string sub closing token, add the IsRunTimeStringSubstitutionMetadata metadata
        Map<String, List<MetaDataValue<?>>> metadataMap = new HashMap<>();
        if (runTimeStringSubClosing > runTimeStringSubLocation) {
            metadataMap.put(IsRunTimeStringSubstitutionMetadata.RUN_TIME_STRING_SUBSTITUTION,
                List.of(new IsRunTimeStringSubstitutionMetadata(true)));
            metadataMap.put(IsNoCacheMetadata.NO_CACHE,
                List.of(new IsNoCacheMetadata(true)));
        }

        return GResultOf.resultOf(new LeafNode(leafValue, metadataMap), List.of());
    }
}
