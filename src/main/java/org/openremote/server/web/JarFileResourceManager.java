package org.openremote.server.web;

import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.PathResource;
import io.undertow.server.handlers.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.*;

/**
 * An Undertow resource manager that can also locate files inside a JAR.
 */
public class JarFileResourceManager extends FileResourceManager {

    private static final Logger LOG = LoggerFactory.getLogger(JarFileResourceManager.class);

    public JarFileResourceManager(File base, long transferMinSize) {
        super(base, transferMinSize);
    }

    public JarFileResourceManager(File base, long transferMinSize, boolean caseSensitive) {
        super(base, transferMinSize, caseSensitive);
    }

    public JarFileResourceManager(File base, long transferMinSize, boolean followLinks, String... safePaths) {
        super(base, transferMinSize, followLinks, safePaths);
    }

    public JarFileResourceManager(long transferMinSize, boolean caseSensitive, boolean followLinks, String... safePaths) {
        super(transferMinSize, caseSensitive, followLinks, safePaths);
    }

    public JarFileResourceManager(File base, long transferMinSize, boolean caseSensitive, boolean followLinks, String... safePaths) {
        super(base, transferMinSize, caseSensitive, followLinks, safePaths);
    }

    @Override
    public Resource getResource(String p) {

        // Not a JAR, use the regular resolution strategy
        if (!base.endsWith(".jar/")) {
            return super.getResource(p);
        }

        // It's a JAR, open it as a new FS and locate the file (no case-check)
        try {
            Path jarPath = Paths.get(base.substring(0, base.length()-1));
            FileSystem jarFS = FileSystems.newFileSystem(jarPath, null);
            Path file = jarFS.getPath(p);
            if (Files.exists(file)) {
                return new PathResource(file, this, p);
            }
            return null;
        } catch (Exception ex) {
            LOG.warn("Error getting document resource: " + p, ex);
            return null;
        }
    }

}
