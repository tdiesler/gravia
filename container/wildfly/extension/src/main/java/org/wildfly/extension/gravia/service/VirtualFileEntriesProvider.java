/*
 * #%L
 * Gravia :: Runtime :: Embedded
 * %%
 * Copyright (C) 2013 JBoss by Red Hat
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.wildfly.extension.gravia.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.gravia.runtime.spi.ModuleEntriesProvider;
import org.jboss.gravia.utils.NotNullException;
import org.jboss.vfs.VirtualFile;

/**
 * A provider for module entries that delegates to
 * the given virtual file.
 *
 * @author thomas.diesler@jboss.com
 * @since 22-Nov-2013
 */
public class VirtualFileEntriesProvider implements ModuleEntriesProvider {

    private final VirtualFile rootFile;

    public VirtualFileEntriesProvider(ResourceRoot resourceRoot) {
        NotNullException.assertValue(resourceRoot, "resourceRoot");
        this.rootFile = resourceRoot.getRoot();
    }

    @Override
    public URL getEntry(String path) {
        try {
            VirtualFile child = rootFile.getChild(path);
            return child.asFileURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public List<String> getEntryPaths(String path) {
        VirtualFile pathChild = rootFile.getChild(path);
        List<VirtualFile> entries = pathChild.getChildren();
        List<String> result = new ArrayList<String>();
        for (VirtualFile entry : entries) {
            result.add(entry.getPathName());
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<URL> findEntries(String path, String filePattern, boolean recurse) {

        if (filePattern == null)
            filePattern = "*";

        List<URL> result = new ArrayList<URL>();
        VirtualFile pathChild = rootFile.getChild(path);
        Pattern pattern = convertToPattern(filePattern);
        fillResultList(pathChild, pattern, recurse, result);
        return Collections.unmodifiableList(result);
    }

    private void fillResultList(VirtualFile pathChild, Pattern pattern, boolean recurse, List<URL> result) {
        List<VirtualFile> entries = pathChild.getChildren();
        for(VirtualFile vfile : entries) {
            String resname = vfile.getPathName();
            if (resname.startsWith("/")) {
                resname = resname.substring(1);
            }
            int lastIndex = resname.lastIndexOf('/');
            String filename = lastIndex > 0 ? resname.substring(lastIndex + 1) : resname;
            if (pattern.matcher(filename).matches()) {
                try {
                    result.add(vfile.asFileURL());
                } catch (MalformedURLException e) {
                    // ignore
                }
                if (recurse) {
                    fillResultList(vfile, pattern, recurse, result);
                }
            }
        }
    }

    // Convert file pattern (RFC 1960-based Filter) into a RegEx pattern
    private Pattern convertToPattern(String filePattern) {
        filePattern = filePattern.replace("*", ".*");
        return Pattern.compile("^" + filePattern + "$");
    }
}