/*
 * #%L
 * Gravia :: Container :: Tomcat :: Support
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
package org.jboss.gravia.container.tomcat.support;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.jboss.gravia.runtime.spi.ModuleEntriesProvider;
import org.jboss.gravia.utils.NotNullException;

/**
 * A provider for module entries that delegates to
 * the given servlet context
 *
 * @author thomas.diesler@jboss.com
 * @since 22-Nov-2013
 */
public class ServletContextEntriesProvider implements ModuleEntriesProvider {

    private final ServletContext servletContext;

    public ServletContextEntriesProvider(ServletContext servletContext) {
        NotNullException.assertValue(servletContext, "servletContext");
        this.servletContext = servletContext;
    }

    @Override
    public URL getEntry(String path) {
        try {
            return servletContext.getResource(path);
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    @Override
    public List<String> getEntryPaths(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<URL> findEntries(String path, String filePattern, boolean recurse) {
        NotNullException.assertValue(path, "path");
        if (recurse == true)
            throw new UnsupportedOperationException("Cannot handle recursive resource discovery");

        if (!path.startsWith("/"))
            path = "/" + path;
        if (filePattern == null)
            filePattern = "*";

        List<URL> result = new ArrayList<URL>();
        Set<String> paths = servletContext.getResourcePaths(path);
        if (paths != null) {
            Pattern pattern = convertToPattern(filePattern);
            for (String childPath : paths) {
                int index = childPath.lastIndexOf('/');
                String filename = index >= 0 ? childPath.substring(index + 1) : childPath;
                if (pattern.matcher(filename).matches()) {
                    try {
                        URL resurl = servletContext.getResource(path + "/" + filename);
                        result.add(resurl);
                    } catch (MalformedURLException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
        }

        return Collections.unmodifiableList(result);
    }

    // Convert file pattern (RFC 1960-based Filter) into a RegEx pattern
    private Pattern convertToPattern(String filePattern) {
        filePattern = filePattern.replace("*", ".*");
        return Pattern.compile("^" + filePattern + "$");
    }
}
