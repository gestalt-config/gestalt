package org.github.gestalt.dotenv.source;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.EnvironmentConfigSource;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Config source to load from a .env file using the dotenv library.
 * It is the responsibility of the user to configure the Dotenv instance setup and load the .env file.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class DotenvConfigSource implements ConfigSource {

    private final Dotenv dotenv;
    private final Dotenv.Filter filter;
    private final String format;
    private final UUID id = UUID.randomUUID();

    /**
     * Config source to load from a .env file using the dotenv library.
     * It is the responsibility of the user to configure the Dotenv instance setup and load the .env file.
     *
     * @param dotenv the dotenv instance
     */
    public DotenvConfigSource(Dotenv dotenv) {
        this(dotenv, null, EnvironmentConfigSource.ENV_VARS);
    }

    /**
     * Config source to load from a .env file using the dotenv library.
     * It is the responsibility of the user to configure the Dotenv instance setup and load the .env file.
     *
     * @param dotenv the dotenv instance
     * @param filter optional filter to filter which entries to load
     */
    public DotenvConfigSource(Dotenv dotenv, Dotenv.Filter filter) {
        this(dotenv, filter, EnvironmentConfigSource.ENV_VARS);
    }

    /**
     * Config source to load from a .env file using the dotenv library.
     * It is the responsibility of the user to configure the Dotenv instance setup and load the .env file.
     *
     * @param dotenv the dotenv instance
     * @param format the format of the data, defaults to load like environment variables.
     *               If you want to load like a properties file you can specify "properties"
     */
    public DotenvConfigSource(Dotenv dotenv, String format) {
        this(dotenv, null, format);
    }

    public DotenvConfigSource(Dotenv dotenv, Dotenv.Filter filter, String format) {
        this.dotenv = dotenv;
        this.filter = filter;
        this.format = format;
    }


    @Override
    public String name() {
        return "dotEnv";
    }

    @Override
    public String format() {
        return format;
    }

    @Override
    public boolean hasStream() {
        return false;
    }

    @Override
    public InputStream loadStream() {
        throw new UnsupportedOperationException("DotEnvConfigSource does not support loadStream");
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public List<Pair<String, String>> loadList() throws GestaltException {

        Set<DotenvEntry> dotEnvEntries;
        if (filter != null) {
            dotEnvEntries = dotenv.entries(filter);
        } else {
            dotEnvEntries = dotenv.entries();
        }

        return dotEnvEntries.stream().map(it -> new Pair<>(it.getKey(), it.getValue())).collect(Collectors.toList());
    }

    @Override
    public UUID id() { //NOPMD
        return null;
    }

    @Override
    public Tags getTags() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DotenvConfigSource)) {
            return false;
        }
        DotenvConfigSource that = (DotenvConfigSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
