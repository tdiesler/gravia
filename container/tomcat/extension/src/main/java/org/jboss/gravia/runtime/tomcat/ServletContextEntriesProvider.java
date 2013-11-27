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
package org.jboss.gravia.runtime.tomcat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

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
    public Enumeration<String> getEntryPaths(String path) {
        throw new UnsupportedOperationException("Bundle.getEntryPaths(String)");
    }

    @Override
    public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
        if (filePattern.contains("*") || recurse == true)
            throw new UnsupportedOperationException("Bundle.getEntryPaths(String,String,boolean)");

        URL result;
        try {
            result = servletContext.getResource(path + "/" + filePattern);
        } catch (MalformedURLException e) {
            result = null;
        }

        if (result == null)
            return null;

        Vector<URL> vector = new Vector<URL>();
        vector.add(result);
        return vector.elements();
    }
}