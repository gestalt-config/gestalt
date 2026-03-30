package org.github.gestalt.config.jfr.config;

import org.github.gestalt.config.entity.GestaltModuleConfig;

/**
 * Java Flight Recorder (JFR) specific configuration.
 * This module records configuration access events to JFR for monitoring and profiling.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class JfrModuleConfig implements GestaltModuleConfig {

    private final boolean includePath;
    private final boolean includeClass;
    private final boolean includeOptional;
    private final boolean includeTags;
    private final String eventLabel;

    public JfrModuleConfig(Boolean includePath, Boolean includeClass, Boolean includeOptional,
                          boolean includeTags, String eventLabel) {
        this.includePath = includePath;
        this.includeClass = includeClass;
        this.includeOptional = includeOptional;
        this.includeTags = includeTags;
        this.eventLabel = eventLabel;
    }

    @Override
    public String name() {
        return "jfr";
    }

    /**
     * Get if we should be including the path when recording a config access event.
     * Warning this is a high cardinality field and should be used carefully.
     *
     * @return if we should be including the path in JFR events.
     */
    public boolean isIncludePath() {
        return includePath;
    }

    /**
     * Get if we should be including the class we asked for when recording a config access event.
     * Warning this is a high cardinality field and should be used carefully.
     *
     * @return if we should be including the class in JFR events.
     */
    public boolean isIncludeClass() {
        return includeClass;
    }

    /**
     * Get if we should be including if the configuration was optional (ie get Optional or get with a default)
     * when recording a config access event.
     *
     * @return if we should be including if the configuration was optional
     */
    public boolean isIncludeOptional() {
        return includeOptional;
    }

    /**
     * Get if we should be including any tags provided when recording a config access event.
     *
     * @return if we should be including any tags provided in JFR events.
     */
    public boolean isIncludeTags() {
        return includeTags;
    }

    /**
     * Get the event label used in JFR events.
     *
     * @return the event label
     */
    public String getEventLabel() {
        return eventLabel;
    }
}
