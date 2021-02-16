package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.utils.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Load a config source from a URL. A format for the data in the string must also be provided.
 *
 * @author Colin Redmond
 */
public class URLConfigSource implements ConfigSource {
    private final String sourceURL;
    private final URL source;
    private final UUID id = UUID.randomUUID();

    public URLConfigSource(String sourceURL) throws GestaltException {
        this.sourceURL = sourceURL;
        if (this.sourceURL == null) {
            throw new GestaltException("The url string provided was null");
        }

        try {
            source = new URL(this.sourceURL);
        } catch (MalformedURLException e) {
            throw new GestaltException("Exception creating URL " + sourceURL + ", with error: " + e.getMessage(), e);
        }
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

    protected String format(String url) {
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
