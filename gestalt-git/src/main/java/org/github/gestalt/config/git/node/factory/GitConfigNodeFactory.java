package org.github.gestalt.config.git.node.factory;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.git.GitConfigSourceBuilder;
import org.github.gestalt.config.git.config.GitModuleConfig;
import org.github.gestalt.config.loader.ConfigLoaderService;
import org.github.gestalt.config.loader.ConfigLoaderUtils;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.factory.ConfigNodeFactory;
import org.github.gestalt.config.node.factory.ConfigNodeFactoryConfig;
import org.github.gestalt.config.utils.GResultOf;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating an Azure Blob Config Node from parameters.
 *
 * <p>Load a config source from a File then converts it to a config node
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class GitConfigNodeFactory implements ConfigNodeFactory {
    public static final String SOURCE_TYPE = "git";
    public static final String PARAMETER_BUCKET_REPO_URI = "repoURI";
    public static final String PARAMETER_OBJECT_BRANCH = "branch";
    public static final String PARAMETER_CONFIG_FILE_PATH = "configFilePath";
    public static final String PARAMETER_LOCAL_REPO_PATH = "localRepoDirectory";
    private static final System.Logger logger = System.getLogger(GitConfigNodeFactory.class.getName());

    private ConfigLoaderService configLoaderService;
    private CredentialsProvider credentials;
    private SshSessionFactory sshSessionFactory;

    @Override
    public void applyConfig(ConfigNodeFactoryConfig config) {
        this.configLoaderService = config.getConfigLoaderService();
        GitModuleConfig moduleConfig = config.getConfig().getModuleConfig(GitModuleConfig.class);

        if (moduleConfig == null) {
            logger.log(System.Logger.Level.DEBUG, "GitConfigNodeFactory has not been registered. " +
                "if you wish to use the Git module with a the include node though  " +
                "${include}=source=git,repoURI=https://raw.githubusercontent.com/gestalt-config/gestalt,localRepoDirectory=\\my\\repo," +
                "configFilePath=/main/gestalt-git/src/test/resources/dev.properties,branch=main" +
                "then you must register an GitConfigNodeFactory config moduleConfig using the builder.");
        } else {
            boolean sshSessionFactorySet = false;
            boolean credentialProviderSet = false;

            if (moduleConfig.hasSshSessionFactory()) {
                sshSessionFactory = moduleConfig.getSshSessionFactory();
                sshSessionFactorySet = true;
            } else {
                logger.log(System.Logger.Level.DEBUG, "GitConfigNodeFactory was registered but the sshSessionFactory " +
                    "was not provided. Please ");
            }

            if (moduleConfig.hasCredentialsProvider()) {
                credentials = moduleConfig.getCredentials();
                credentialProviderSet = true;
            }

            if (!sshSessionFactorySet && !credentialProviderSet) {
                logger.log(System.Logger.Level.WARNING, "GitConfigNodeFactory was registered but the sshSessionFactory and " +
                    "credentials was not provided. Will be unable to use Git include nodes in private repos.");
            }
        }
    }

    @Override
    public Boolean supportsType(String type) {
        return SOURCE_TYPE.equalsIgnoreCase(type);
    }

    @Override
    public GResultOf<List<ConfigNode>> build(Map<String, String> parameters) {
        var configSourceBuilder = GitConfigSourceBuilder.builder();

        List<ValidationError> errors = new ArrayList<>();
        try {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                switch (entry.getKey()) {
                    case PARAMETER_BUCKET_REPO_URI:
                        configSourceBuilder.setRepoURI(entry.getValue());
                        break;
                    case PARAMETER_OBJECT_BRANCH:
                        configSourceBuilder.setBranch(entry.getValue());
                        break;
                    case PARAMETER_CONFIG_FILE_PATH:
                        configSourceBuilder.setConfigFilePath(entry.getValue());
                        break;
                    case PARAMETER_LOCAL_REPO_PATH:
                        configSourceBuilder.setLocalRepoDirectory(Path.of(entry.getValue()));
                        break;
                    default:
                        errors.add(
                            new ValidationError.ConfigSourceFactoryUnknownParameter(SOURCE_TYPE, entry.getKey(), entry.getValue()));
                        break;
                }
            }

            if (credentials != null) {
                configSourceBuilder.setCredentials(credentials);
            }

            if (sshSessionFactory != null) {
                configSourceBuilder.setSshSessionFactory(sshSessionFactory);
            }

            var fileConfigSource = configSourceBuilder.build().getConfigSource();

            GResultOf<List<ConfigNode>> loadedNodes = ConfigLoaderUtils.convertSourceToNodes(fileConfigSource, configLoaderService);
            errors.addAll(loadedNodes.getErrors());

            return GResultOf.resultOf(loadedNodes.results(), errors);
        } catch (Exception ex) {
            errors.add(new ValidationError.ConfigSourceFactoryException(SOURCE_TYPE, ex));
            return GResultOf.errors(errors);
        }
    }
}
