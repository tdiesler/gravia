/*
 * #%L
 * Gravia :: Runtime :: API
 * %%
 * Copyright (C) 2013 - 2014 JBoss by Red Hat
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
