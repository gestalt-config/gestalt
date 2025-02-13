package org.github.gestalt.config.decoder;

/**
 * Enumeration of all modes for the proxy decoder.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public enum ProxyDecoderMode {

    // Values are cached and saved in the proxy object to be returned. Does not refresh if the configs are reloaded.
    CACHE,

    // Calls the gestalt library to get the proxy value for each method call. Will get the most recent values but have more overhead.
    PASSTHROUGH
}
