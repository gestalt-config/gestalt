package org.github.gestalt.config.reload;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.FileConfigSource;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * File change reload strategy for listening for local file changes.
 *
 * <p>Listens for local file changes including symlink changes.
 *
 * <p>Creates a thread in the background to watch for file changes.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class FileChangeReloadStrategy extends ConfigReloadStrategy {
    private static final System.Logger logger = System.getLogger(FileChangeReloadStrategy.class.getName());
    private Path path;

    private WatchService watcher;

    private final ExecutorService executor;

    private volatile boolean isWatching = false;

    /**
     * constructor.
     *
     * @throws GestaltConfigurationException if this is not a file source or other errors.
     */
    public FileChangeReloadStrategy() throws GestaltConfigurationException {
        this(null, Executors.newSingleThreadExecutor());
    }

    /**
     * constructor.
     *
     * @param executor executor to run the watcher on.
     * @throws GestaltConfigurationException if this is not a file source or other errors.
     */
    public FileChangeReloadStrategy(ExecutorService executor) throws GestaltConfigurationException {
        this(null, executor);
    }

    /**
     * constructor.
     *
     * @param source the source to watch for reload
     * @throws GestaltConfigurationException if this is not a file source or other errors.
     */
    public FileChangeReloadStrategy(ConfigSource source) throws GestaltConfigurationException {
        this(source, Executors.newSingleThreadExecutor());
    }


    /**
     * constructor.
     *
     * @param source   the source to watch for reload
     * @param executor ExecutorService to get thread from.
     * @throws GestaltConfigurationException if this is not a file source or other errors.
     */
    public FileChangeReloadStrategy(ConfigSource source, ExecutorService executor) throws GestaltConfigurationException {
        super(source);
        this.executor = executor;
        if (source != null && !(source instanceof FileConfigSource)) {
            throw new GestaltConfigurationException("Unable to add a File Change reload strategy to a non file source " + source);
        }
        setupWatcherTask();
    }

    @Override
    public void setSource(ConfigSource source) throws GestaltConfigurationException {
        if (!(source instanceof FileConfigSource)) {
            throw new GestaltConfigurationException("Unable to add a File Change reload strategy to a non file source " + source);
        }
        this.source = source;
        setupWatcherTask();
    }

    private void setupWatcherTask() throws GestaltConfigurationException {
        if (source != null) {
            path = ((FileConfigSource) source).getPath();
            try {
                if (watcher != null) {
                    watcher.close();
                }

                watcher = FileSystems.getDefault().newWatchService();
                path.toAbsolutePath().getParent()
                    .register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (IOException e) {
                throw new GestaltConfigurationException("unable to create a watch service on file " + path);
            }
        }
    }

    @Override
    public void registerListener(ConfigReloadListener listener) {
        super.registerListener(listener);
        if (!isWatching) {
            isWatching = true;
            executor.execute(this::fileWatchTask);
        }
    }

    @Override
    public void removeListener(ConfigReloadListener listener) {
        super.removeListener(listener);
        if (listeners.isEmpty() && isWatching) {
            isWatching = false;
        }
    }

    // inspiration taken from:
    // https://github.com/jdiazcano/cfg4k/blob/master/cfg4k-core/src/main/kotlin/com/jdiazcano/cfg4k/reloadstrategies/FileChangeReloadStrategy.kt
    // if its a symlink, then we should also watch symlinks for changes, this supports Kubernetes-style ConfigMap resources e.g.
    //   configfile -> ..data/configfile
    //   ..data -> ..2019_09_20_05_25_13.543205648
    //   ..2019_09_20_05_25_13.543205648/configfile
    // Here, Kubernetes creates a new timestamped directory when the configmap changes, and just modifies the ..data symlink to point to it
    // FileWatcher raises symlink changes (e.g. overwrite an existing link target on Linux via `ln -sfn`) as ENTRY_CREATE
    // we don't use toRealPath() here, because we *want* the parent the file appears to be in, not the file's real parent
    private void fileWatchTask() {
        try {
            WatchKey key;
            while (isWatching) {
                key = watcher.take();
                try {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                            break;
                        }

                        Path fileName = (Path) event.context();

                        // if any entry in the chain of symbolic links leading to the actual file, including the actual
                        // file itself, has been created/modified, reload
                        List<Path> linkChain = new ArrayList<>();
                        Path currentPath = path;
                        linkChain.add(currentPath);
                        while (Files.isSymbolicLink(currentPath)) {
                            Path nextSymLink = Files.readSymbolicLink(currentPath).iterator().next();
                            currentPath = currentPath.getParent().resolve(nextSymLink);
                            linkChain.add(currentPath);
                        }

                        Path parentFile = path.getParent().resolve(fileName);
                        if (linkChain.contains(parentFile)) {
                            reload();
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                } catch (IOException | GestaltException e) {
                    logger.log(System.Logger.Level.ERROR,
                        "Ignoring exception while watching for file " + path + ", message: " + e.getMessage(), e);
                }
            }
        } catch (InterruptedException e) {
            logger.log(System.Logger.Level.ERROR,
                "Received a InterruptedException while watching file " + path.toString() + ", message: " + e.getMessage(), e);
        }
    }
}
