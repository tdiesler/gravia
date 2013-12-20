/*
 * #%L
 * Gravia :: Runtime :: API
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
package org.jboss.gravia.runtime.spi;

import java.net.URL;
import java.util.List;

import org.jboss.gravia.runtime.Module;

/**
 * A provider for module entries.
 *
 * <p>
 * To discover and return module entries the runtime must delegate to specifc module type dependent implementations
 *
 * <ul>
 * <li><strong>OSGi:</strong> The {@code Bundle} entries API
 * <li><strong>Tomcat:</strong> The {@code ServletContext} resource API
 * <li><strong>WildFly:</strong> The JBoss {@code Module}s entries API
 * </ul>
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public interface ModuleEntriesProvider {

    /**
     * Returns an Enumeration of all the paths ({@code String} objects) to
     * entries within this module whose longest sub-path matches the specified
     * path.
     *
     * @see Module#getEntryPaths(String)
     */
    List<String> getEntryPaths(String path);

    /**
     * Returns a URL to the entry at the specified path in this module. This
     * module's class loader is not used to search for the entry. Only the
     * contents of this module are searched for the entry.
     *
     * @see Module#getEntry(String)
     */
    URL getEntry(String path);

    /**
     * Find entries in this module. This module's
     * class loader is not used to search for entries.
     *
     * @see Module#findEntries(String, String, boolean)
     */
    List<URL> findEntries(String path, String filePattern, boolean recurse);
}
