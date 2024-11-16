package org.github.gestalt.config.metadata;

import java.util.List;
import java.util.Map;

public class IsRunTimeStringSubstitutionMetadata extends MetaDataValue<Boolean> {
    public static String RUN_TIME_STRING_SUBSTITUTION = "runTimeStringSubstitution";

    public IsRunTimeStringSubstitutionMetadata(Boolean value) {
        super(value);
    }

    @Override
    public String keyValue() {
        return RUN_TIME_STRING_SUBSTITUTION;
    }

    // We do not want to rollup the IsSecretMetadata to the calling metadata
    @Override
    public Map<String, List<MetaDataValue<?>>> rollup(Map<String, List<MetaDataValue<?>>> metadata) {
        return metadata;
    }
}
