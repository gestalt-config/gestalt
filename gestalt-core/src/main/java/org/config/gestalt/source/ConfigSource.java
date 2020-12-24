package org.config.gestalt.source;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.utils.Pair;

import java.io.InputStream;
import java.util.List;

/**
 * Interface to define a stream config loader.
 */
public interface ConfigSource {
    /**
     * if this config source has a stream to load from.
     *
     * @return if this config source has a stream to load from
     */
    boolean hasStream();

    /**
     * If this config source has a stream, this will return the stream of data.
     * Or if not supported it will return a empty option.
     *
     * @return input stream of data
     * @throws GestaltException if there are any IO or if this is an unsupported operation
     */
    InputStream loadStream() throws GestaltException;

    /**
     * if this config source provides a list of config values.
     *
     * @return if this config source provides a list of config values
     */
    boolean hasList();

    /**
     * provides a list of config values.
     *
     * @return provides a list of config values
     * @throws GestaltException if there are any IO or if this is an unsupported operation
     */
    List<Pair<String, String>> loadList() throws GestaltException;

    /**
     * The format of the config source, for example this can be envVars, the extension of a file (properties, json, ect).
     *
     * @return The format of the config source
     */
    String format();

    /**
     * human readable name for logging.
     *
     * @return human readable name for logging
     */
    String name();
}
