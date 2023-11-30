package org.github.gestalt.config.reload;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.FileConfigSource;
import org.github.gestalt.config.source.StringConfigSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

class FileChangeReloadStrategyTest {

    @Test
    public void changeContentsOfFile() throws GestaltException, IOException, InterruptedException {
        Path path;
        path = Files.createTempFile("gestalt", "test.properties");
        path.toFile().deleteOnExit();
        Files.writeString(path, "user=userA");

        FileConfigSource source = new FileConfigSource(path);
        ConfigReloadStrategy strategy = new FileChangeReloadStrategy();
        strategy.setSource(source);

        ConfigListener listener = new ConfigListener();
        strategy.registerListener(listener);

        Files.writeString(path, "user=userB");

        for (int i = 0; i < 5; i++) {
            if (listener.count > 1) {
                break;
            } else {
                Thread.sleep(10);
            }
        }

        Assertions.assertTrue(listener.count >= 1);
        strategy.removeListener(listener);

        Files.writeString(path, "user=userC");

        Thread.sleep(100);

        int previousCount = listener.count;
        Assertions.assertEquals(previousCount, listener.count);
    }

    @Test
    public void changeContentsOfFileWithSubDir() throws GestaltException, IOException, InterruptedException {
        Path folder = Files.createTempDirectory("gestalt");
        Path path = folder.resolve("reloadedfile.properties");

        folder.toFile().mkdirs();
        folder.toFile().deleteOnExit();

        Files.writeString(path, "user=userA");

        FileConfigSource source = new FileConfigSource(path);
        ConfigReloadStrategy strategy = new FileChangeReloadStrategy(source);

        ConfigListener listener = new ConfigListener();
        strategy.registerListener(listener);

        Thread.sleep(100);
        Files.writeString(path, "user=userB");

        for (int i = 0; i < 5; i++) {
            if (listener.count > 1) {
                break;
            } else {
                Thread.sleep(10);
            }
        }

        Assertions.assertTrue(listener.count >= 1);

        strategy.removeListener(listener);

        Files.writeString(path, "user=userC");

        Thread.sleep(100);

        int previousCount = listener.count;
        Assertions.assertEquals(previousCount, listener.count);
    }

    //to run this test it must be run as an administrator.
    @Test
    @Disabled
    public void changeContentsOfFileWithSymlinkChain() throws GestaltException, IOException, InterruptedException {
        Path folder = Files.createTempDirectory("gestalt");

        folder.toFile().mkdirs();
        folder.toFile().deleteOnExit();

        Path numbered1 = Files.createDirectory(folder.resolve("..10001"));
        Path numbered2 = Files.createDirectory(folder.resolve("..10002"));
        Path dataLn = Files.createSymbolicLink(folder.resolve("..data"), folder.relativize(numbered1));

        Path file1 = numbered1.resolve("reloadedfile.properties");
        Files.writeString(file1, "user=userA");

        Path file2 = numbered2.resolve("reloadedfile.properties");
        Files.writeString(file2, "user=userB");

        Path configFileLn = Files.createSymbolicLink(folder.resolve("reloadedfile.properties"),
            folder.relativize(dataLn).resolve("reloadedfile.properties"));

        FileConfigSource source = new FileConfigSource(configFileLn);
        ConfigReloadStrategy strategy = new FileChangeReloadStrategy(source);

        ConfigListener listener = new ConfigListener();
        strategy.registerListener(listener);

        Files.delete(dataLn);
        Files.createSymbolicLink(folder.resolve("..data"), folder.relativize(numbered2));
        for (int i = 0; i < 5; i++) {
            if (listener.count >= 1) {
                break;
            } else {
                Thread.sleep(100);
            }
        }

        Assertions.assertTrue(listener.count >= 1);

        strategy.removeListener(listener);
        // change the ..data link like Kubernetes does
        Files.delete(dataLn);
        Files.createSymbolicLink(folder.resolve("..data"), folder.relativize(numbered1));

        int previousCount = listener.count;
        Thread.sleep(100);

        Assertions.assertEquals(previousCount, listener.count);
    }

    @Test
    public void wrongSourceConstructor() {
        GestaltConfigurationException ex = Assertions.assertThrows(GestaltConfigurationException.class,
            () -> new FileChangeReloadStrategy(new StringConfigSource("abc=def", "properties")));

        Assertions.assertTrue(ex.getMessage().startsWith("Unable to add a File Change reload strategy to a non file source"));
    }

    @Test
    public void wrongSourceSet() throws GestaltConfigurationException {
        ConfigReloadStrategy strategy = new FileChangeReloadStrategy(Executors.newSingleThreadExecutor());

        GestaltConfigurationException ex = Assertions.assertThrows(GestaltConfigurationException.class,
            () -> strategy.setSource(new StringConfigSource("abc=def", "properties")));

        Assertions.assertTrue(ex.getMessage().startsWith("Unable to add a File Change reload strategy to a non file source"));
    }

    private static class ConfigListener implements ConfigReloadListener {

        public int count = 0;

        @Override
        public void reload(ConfigSource source) {
            count++;
        }
    }
}
