/*
 * #%L
 * Gravia :: Container :: WildFly :: Extension
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.jboss.gravia.utils.IllegalArgumentAssertion;
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
        IllegalArgumentAssertion.assertNotNull(resourceRoot, "resourceRoot");
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
