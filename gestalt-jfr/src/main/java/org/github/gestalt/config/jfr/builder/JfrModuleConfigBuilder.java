package org.github.gestalt.config.jfr.builder;

import org.github.gestalt.config.entity.GestaltModuleConfig;
import org.github.gestalt.config.jfr.config.JfrModuleConfig;

/**
 * Module config for JFR (Java Flight Recorder). If you wish to customize JFR event recording,
 * register the results of this builder with {@link org.github.gestalt.config.builder.GestaltBuilder#addModuleConfig(GestaltModuleConfig)}.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class JfrModuleConfigBuilder {

    private Boolean includePath = false;
    private Boolean includeClass = false;
    private Boolean includeOptional = false;
    private Boolean includeTags = false;
    private String eventLabel = "Gestalt Config Access";

    private JfrModuleConfigBuilder() {
    }

    /**
     * Create a builder to create the JFR config.
     *
     * @return a builder to create the JFR config.
     */
    public static JfrModuleConfigBuilder builder() {
        return new JfrModuleConfigBuilder();
    }

    /**
     * Build the JfrModuleConfig.
     *
     * @return the JfrModuleConfig.
     */
    public JfrModuleConfig build() {
        return new JfrModuleConfig(includePath, includeClass, includeOptional, includeTags, eventLabel);
    }

    /**
     * Get if we should be including the path in JFR events.
     * Warning this is a high cardinality field and should be used carefully.
     *
     * @return if we should be including the path.
     */
    public Boolean getIncludePath() { // NOPMD
        return includePath;
    }

    /**
     * Set if we should be including the path in JFR events.
     * Warning this is a high cardinality field and should be used carefully.
     *
     * @param includePath if we should be including the path.
     * @return the builder
     */
    public JfrModuleConfigBuilder setIncludePath(Boolean includePath) {
        this.includePath = includePath;
        return this;
    }

    /**
     * Get if we should be including the class in JFR events.
     * Warning this is a high cardinality field and should be used carefully.
     *
     * @return if we should be including the class.
     */
    public Boolean getIncludeClass() { // NOPMD
        return includeClass;
    }

    /**
     * Set if we should be including the class in JFR events.
     * Warning this is a high cardinality field and should be used carefully.
     *
     * @param includeClass if we should be including the class.
     * @return the builder
     */
    public JfrModuleConfigBuilder setIncludeClass(Boolean includeClass) {
        this.includeClass = includeClass;
        return this;
    }

    /**
     * Get if we should be including if the configuration was optional in JFR events.
     *
     * @return if we should be including optional flag.
     */
    public Boolean getIncludeOptional() { // NOPMD
        return includeOptional;
    }

    /**
     * Set if we should be including if the configuration was optional in JFR events.
     *
     * @param includeOptional if we should be including optional flag.
     * @return the builder
     */
    public JfrModuleConfigBuilder setIncludeOptional(Boolean includeOptional) {
        this.includeOptional = includeOptional;
        return this;
    }

    /**
     * Get if we should be including the tags in JFR events.
     *
     * @return if we should be including tags.
     */
    public Boolean getIncludeTags() { // NOPMD
        return includeTags;
    }

    /**
     * Set if we should be including tags in JFR events.
     *
     * @param includeTags if we should be including tags.
     * @return the builder
     */
    public JfrModuleConfigBuilder setIncludeTags(Boolean includeTags) {
        this.includeTags = includeTags;
        return this;
    }

    /**
     * Get the event label used in JFR events.
     *
     * @return the event label
     */
    public String getEventLabel() {
        return eventLabel;
    }

    /**
     * Set the event label used in JFR events.
     *
     * @param eventLabel the event label
     * @return the builder
     */
    public JfrModuleConfigBuilder setEventLabel(String eventLabel) {
        this.eventLabel = eventLabel;
        return this;
    }
}
