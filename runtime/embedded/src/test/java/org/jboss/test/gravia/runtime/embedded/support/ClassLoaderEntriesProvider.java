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
package org.jboss.test.gravia.runtime.embedded.support;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.spi.ModuleEntriesProvider;
import org.jboss.gravia.utils.NotNullException;

/**
 * A provider for module entries that delegates to
 * the given module class loader
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
class ClassLoaderEntriesProvider implements ModuleEntriesProvider {

    private final ClassLoader classLoader;

    ClassLoaderEntriesProvider(Module module) {
        NotNullException.assertValue(module, "module");
        classLoader = module.adapt(ClassLoader.class);
    }

    @Override
    public URL getEntry(String path) {
        // [TODO] flawed because of parent first access
        return classLoader.getResource(path);
    }

    @Override
    public List<String> getEntryPaths(String path) {
        throw new UnsupportedOperationException("Bundle.getEntryPaths(String)");
    }

    @Override
    public List<URL> findEntries(String path, String filePattern, boolean recurse) {
        if (filePattern.contains("*") || recurse == true)
            throw new UnsupportedOperationException("Bundle.getEntryPaths(String,String,boolean)");

        // [TODO] flawed because of parent first access
        URL result = classLoader.getResource(path + "/" + filePattern);

        return result != null ? Collections.singletonList(result) : Collections.<URL>emptyList();
    }
}