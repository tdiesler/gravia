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
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
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
    public Enumeration<String> getEntryPaths(String path) {
        VirtualFile pathChild = rootFile.getChild(path);
        List<VirtualFile> entries = pathChild.getChildren();
        if (entries.isEmpty())
            return null;

        Vector<String> result = new Vector<String>();
        for (VirtualFile entry : entries) {
            result.add(entry.getPathName());
        }

        return result.elements();
    }

    @Override
    public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
        if (filePattern.contains("*") || recurse == true)
            throw new UnsupportedOperationException("Bundle.getEntryPaths(String,String,boolean)");

        VirtualFile pathChild = rootFile.getChild(path);
        List<VirtualFile> entries = pathChild.getChildren();
        if (entries.isEmpty())
            return null;

        Vector<URL> result = getResultVector(entries, filePattern);
        return result.elements();
    }

    private Vector<URL> getResultVector(List<VirtualFile> entries, String filePattern) {
        Vector<URL> result = new Vector<URL>();
        Pattern pattern = convertToPattern(filePattern);
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
            }
        }
        return result;
    }

    // Convert file pattern (RFC 1960-based Filter) into a RegEx pattern
    private static Pattern convertToPattern(String filePattern) {
        filePattern = filePattern.replace("*", ".*");
        return Pattern.compile("^" + filePattern + "$");
    }
}