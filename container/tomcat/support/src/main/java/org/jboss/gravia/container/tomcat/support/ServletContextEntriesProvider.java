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