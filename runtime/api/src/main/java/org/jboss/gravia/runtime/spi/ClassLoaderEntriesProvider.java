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
import java.util.Collections;
import java.util.List;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.utils.IllegalArgumentAssertion;

/**
 * A provider for module entries that delegates to
 * the given module class loader
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public final class ClassLoaderEntriesProvider implements ModuleEntriesProvider {

    private final ClassLoader classLoader;

    public ClassLoaderEntriesProvider(Module module) {
        IllegalArgumentAssertion.assertNotNull(module, "module");
        classLoader = module.adapt(ClassLoader.class);
    }

    public ClassLoaderEntriesProvider(ClassLoader classLoader) {
        IllegalArgumentAssertion.assertNotNull(classLoader, "classLoader");
        this.classLoader = classLoader;
    }

    @Override
    public URL getEntry(String path) {
        // flawed because of parent first access
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

        // flawed because of parent first access
        URL result = classLoader.getResource(path + "/" + filePattern);
        if (result == null && !path.startsWith("/")) {
            return findEntries("/" + path, filePattern, recurse);
        }

        return result != null ? Collections.singletonList(result) : Collections.<URL>emptyList();
    }
}
