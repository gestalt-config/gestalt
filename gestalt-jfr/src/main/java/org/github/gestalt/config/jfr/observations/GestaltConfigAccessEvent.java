package org.github.gestalt.config.jfr.observations;

import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Category;

/**
 * JFR Event for tracking Gestalt configuration access.
 * Records timing and details about configuration retrieval operations.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
@Label("Gestalt Config Access")
@Description("Configuration value retrieval from Gestalt")
@Category({"Gestalt", "Configuration"})
public class GestaltConfigAccessEvent extends Event {

    @Label("Path")
    @Description("Configuration path being accessed")
    public String path;

    @Label("Class")
    @Description("Target class type for deserialization")
    public String targetClass;

    @Label("Is Optional")
    @Description("Whether the configuration was accessed as optional")
    public boolean isOptional;

    @Label("Tags")
    @Description("Configuration tags applied to this access")
    public String tags;

    @Label("Duration")
    @Description("Time taken to retrieve the configuration")
    public long duration;

    @Label("Success")
    @Description("Whether the configuration retrieval was successful")
    public boolean success;

    @Label("Error")
    @Description("Error message if the retrieval failed")
    public String errorMessage;
}
