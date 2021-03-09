package org.github.gestalt.config.git;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.util.FS;

public class GitSshSessionFactory extends JschConfigSessionFactory {
    private final String privateKey;
    private final String knownHostsFile;

    public GitSshSessionFactory(String privateKey) {
        this(privateKey, null);
    }

    public GitSshSessionFactory(String privateKey, String knownHostsFile) {
        this.privateKey = privateKey;
        this.knownHostsFile = knownHostsFile;
    }

    @Override
    protected void configure(OpenSshConfig.Host host, Session session) {
        if (knownHostsFile == null) {
            session.setConfig("StrictHostKeyChecking", "no");
        }
    }

    @Override
    protected JSch createDefaultJSch(FS fs) throws JSchException {
        JSch defaultJSch = super.createDefaultJSch(fs);
        defaultJSch.removeAllIdentity();
        defaultJSch.addIdentity(privateKey);
        if (knownHostsFile == null) {
            defaultJSch.setKnownHosts(knownHostsFile);
        }
        return defaultJSch;
    }
}
