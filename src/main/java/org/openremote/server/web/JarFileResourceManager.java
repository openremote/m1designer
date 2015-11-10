/*
 * Copyright 2015, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
