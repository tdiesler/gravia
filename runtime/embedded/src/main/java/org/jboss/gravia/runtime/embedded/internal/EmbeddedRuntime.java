/*
 * #%L
 * JBossOSGi Framework
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
package org.jboss.gravia.runtime.embedded.internal;

import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Vector;

import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.PropertiesProvider;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.AbstractRuntime;
import org.jboss.gravia.runtime.spi.ModuleEntriesProvider;
import org.jboss.gravia.runtime.spi.RuntimeEventsHandler;
import org.jboss.gravia.runtime.spi.RuntimePlugin;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public final class EmbeddedRuntime extends AbstractRuntime {

    private final RuntimeServicesHandler serviceManager;
    private final RuntimeStorageHandler storageHandler;

    public EmbeddedRuntime(PropertiesProvider propertiesProvider) {
        super(propertiesProvider);
        serviceManager = new RuntimeServicesHandler(adapt(RuntimeEventsHandler.class));
        storageHandler = new RuntimeStorageHandler(propertiesProvider, true);
    }

    @Override
    public void init() {

        // Install the plugin modules
        List<Module> pluginModules = new ArrayList<Module>();
        ClassLoader classLoader = getClass().getClassLoader();
        ServiceLoader<RuntimePlugin> services = ServiceLoader.load(RuntimePlugin.class, EmbeddedRuntime.class.getClassLoader());
        Iterator<RuntimePlugin> iterator = services.iterator();
        while (iterator.hasNext()) {
            RuntimePlugin plugin = iterator.next();
            try {
                Module module = plugin.installPluginModule(this, classLoader);
                pluginModules.add(module);
            } catch (ModuleException ex) {
                LOGGER.errorf(ex, "Cannot load plugin: %s", plugin.getClass().getName());
            }
        }

        // Start the plugin modules
        for (Module module : pluginModules) {
            try {
                module.start();
            } catch (ModuleException ex) {
                LOGGER.errorf(ex, "Cannot start plugin: %s", module);
            }
        }
    }

    @Override
    public AbstractModule createModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers) {
        return new EmbeddedModule(this, classLoader, resource, headers);
    }

    @Override
    public ModuleEntriesProvider getModuleEntriesProvider(Module module) {
        return new CLassLoaderEntriesProvider(module.adapt(ClassLoader.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A adapt(Class<A> type) {
        A result = super.adapt(type);
        if (result == null) {
            if (type.isAssignableFrom(RuntimeServicesHandler.class)) {
                result = (A) serviceManager;
            } else if (type.isAssignableFrom(RuntimeStorageHandler.class)) {
                result = (A) storageHandler;
            }
        }
        return result;
    }

    @Override
    protected void uninstallModule(Module module) {
        super.uninstallModule(module);
    }

    private class CLassLoaderEntriesProvider implements ModuleEntriesProvider {

        private final ClassLoader classLoader;

        CLassLoaderEntriesProvider(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        @Override
        public URL getEntry(String path) {
            // [TODO] flawed because of parent first access
            return classLoader.getResource(path);
        }

        @Override
        public Enumeration<String> getEntryPaths(String path) {
            throw new UnsupportedOperationException("Bundle.getEntryPaths(String)");
        }

        @Override
        public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
            if (filePattern.contains("*") || recurse == true)
                throw new UnsupportedOperationException("Bundle.getEntryPaths(String,String,boolean)");

            // [TODO] flawed because of parent first access
            URL result = classLoader.getResource(path + "/" + filePattern);
            if (result == null)
                return null;

            Vector<URL> vector = new Vector<URL>();
            vector.add(result);
            return vector.elements();
        }
    }
}
