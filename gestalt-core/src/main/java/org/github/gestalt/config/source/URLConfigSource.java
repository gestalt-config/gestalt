package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Load a config source from a URL. A format for the data in the string must also be provided.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class URLConfigSource implements ConfigSource {
    private final String sourceURL;
    private final URL source;
    private final UUID id = UUID.randomUUID();

    private final Tags tags;

    /**
     * Create a URLConfigSource to load a config from a URL.
     *
     * @param sourceURL source URL
     * @throws GestaltException any exceptions
     */
    public URLConfigSource(String sourceURL) throws GestaltException {
        this(sourceURL, Tags.of());
    }

    /**
     * Create a URLConfigSource to load a config from a URL.
     *
     * @param sourceURL source URL
     * @param tags      tags associated with the source
     * @throws GestaltException any exceptions
     */
    public URLConfigSource(String sourceURL, Tags tags) throws GestaltException {
        this.sourceURL = sourceURL;
        if (this.sourceURL == null) {
            throw new GestaltException("The url string provided was null");
        }

        try {
            source = URI.create(this.sourceURL).toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new GestaltException("Exception creating URL " + sourceURL + ", with error: " + e.getMessage(), e);
        }
        this.tags = tags;
    }

    @Override
    public boolean hasStream() {
        return true;
    }

    @Override
    public InputStream loadStream() throws GestaltException {
        try {
            return source.openStream();
        } catch (IOException e) {
            throw new GestaltException("Exception opening stream to " + sourceURL, e);
        }
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public List<Pair<String, String>> loadList() throws GestaltException {
        throw new GestaltException("Unsupported operation loadList on an URLConfigSource");
    }


    @Override
    public String name() {
        return "URL format: " + sourceURL;
    }

    @Override
    public String format() {
        return format(this.sourceURL);
    }

    /**
     * Finds the extension of a file to get the file format at a URL.
     *
     * @param url the name of the file at a url
     * @return the extension of the file
     */
    private String format(String url) {
        int index = url.lastIndexOf('.');
        if (index > 0) {
            return url.substring(index + 1);
        } else {
            return "";
        }
    }

    @Override
    public UUID id() {  //NOPMD
        return id;
    }

    @Override
    public Tags getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof URLConfigSource)) {
            return false;
        }
        URLConfigSource that = (URLConfigSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
