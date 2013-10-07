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
import java.util.Enumeration;

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
     * path. This module's class loader is not used to search for entries. Only
     * the contents of this module are searched.
     * <p>
     * The specified path is always relative to the root of this module and may
     * begin with a &quot;/&quot;. A path value of &quot;/&quot; indicates the
     * root of this module.
     * <p>
     * Returned paths indicating subdirectory paths end with a &quot;/&quot;.
     * The returned paths are all relative to the root of this module and must
     * not begin with &quot;/&quot;.
     * <p>
     * Note: Jar and zip files are not required to include directory entries.
     * Paths to directory entries will not be returned if the module contents do
     * not contain directory entries.
     *
     * @param path The path name for which to return entry paths.
     * @return An Enumeration of the entry paths ({@code String} objects) or
     *         {@code null} if no entry could be found.
     * @throws IllegalStateException If this module has been uninstalled.
     */
    Enumeration<String> getEntryPaths(String path);

    /**
     * Returns a URL to the entry at the specified path in this module. This
     * module's class loader is not used to search for the entry. Only the
     * contents of this module are searched for the entry.
     * <p>
     * The specified path is always relative to the root of this module and may
     * begin with &quot;/&quot;. A path value of &quot;/&quot; indicates the
     * root of this module.
     * <p>
     * Note: Jar and zip files are not required to include directory entries.
     * URLs to directory entries will not be returned if the module contents do
     * not contain directory entries.
     *
     * @param path The path name of the entry.
     * @return A URL to the entry, or {@code null} if no entry could be found.
     * @throws IllegalStateException If this module has been uninstalled.
     */
    URL getEntry(String path);

    /**
     * Find entries in this module. This module's
     * class loader is not used to search for entries. Only the contents of this
     * module is searched for the specified entries.
     *
     * If this module's state is {@code INSTALLED}, this method must attempt to
     * resolve this module before attempting to find entries.
     *
     * <p>
     * This method is intended to be used to obtain configuration, setup,
     * localization and other information from this module.
     * This &quot;module space&quot; is not a namespace with
     * unique members; the same entry name can be present multiple times. This
     * method therefore returns an enumeration of URL objects. These URLs can
     * come from different JARs but have the same path name. This method can
     * either return only entries in the specified path or recurse into
     * subdirectories returning entries in the directory tree beginning at the
     * specified path.
     * <p>
     *
     * URLs for directory entries must have their path end with &quot;/&quot;.
     * <p>
     * Note: Jar and zip files are not required to include directory entries.
     * URLs to directory entries will not be returned if the module contents do
     * not contain directory entries.
     *
     * @param path The path name in which to look. The path is always relative
     *        to the root of this module and may begin with &quot;/&quot;. A
     *        path value of &quot;/&quot; indicates the root of this module.
     * @param filePattern The file name pattern for selecting entries in the
     *        specified path. The pattern is only matched against the last
     *        element of the entry path. If the entry is a directory then the
     *        trailing &quot;/&quot; is not used for pattern matching. Substring
     *        matching is supported, as specified in the Filter specification,
     *        using the wildcard character (&quot;*&quot;). If null is
     *        specified, this is equivalent to &quot;*&quot; and matches all
     *        files.
     * @param recurse If {@code true}, recurse into subdirectories. Otherwise
     *        only return entries from the specified path.
     * @return An enumeration of URL objects for each matching entry, or
     *         {@code null} if no matching entry could be found.
     * @throws IllegalStateException If this module has been uninstalled.
     */
    Enumeration<URL> findEntries(String path, String filePattern, boolean recurse);
}
