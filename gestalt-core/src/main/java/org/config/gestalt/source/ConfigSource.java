package org.config.gestalt.source;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.utils.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Interface to define a either stream or map based config source.
 * Configs come in two forms, either a stream such as a file.
 * Or a list of values, such as an Environment Variables or a in memory map provided.
 * A source will have a format that a Config loader expects to load.
 *
 * <p>Each source has a unique ID, so we can track and match it in the system.
 *
 * @author Colin Redmond
 */
public interface ConfigSource {

    /**
     * If this config source has a stream to load from.
     *
     * @return if this config source has a stream to load from
     */
    boolean hasStream();

    /**
     * If this config source has a stream, this will return the stream of data.
     * Or if not supported it will throw an exception.
     *
     * @return input stream of data
     * @throws GestaltException if there are any IO or if this is an unsupported operation
     */
    InputStream loadStream() throws GestaltException;

    /**
     * If this config source provides a list of config values.
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
     * @return human readable name for logging.
     */
    String name();

    /**
     * Id that represents this source as unique.
     *
     * @return id
     */
    UUID id();  //NOPMD
}
