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
package org.jboss.gravia.runtime.osgi.internal;

import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;

import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.osgi.OSGiRuntime;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.AbstractRuntime;
import org.jboss.gravia.runtime.spi.ModuleEntriesProvider;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public final class OSGiRuntimeImpl extends AbstractRuntime implements OSGiRuntime {

    private final BundleContext bundleContext;

    public OSGiRuntimeImpl(BundleContext bundleContext, PropertiesProvider propertiesProvider) {
        super(propertiesProvider);
        this.bundleContext = bundleContext;
    }

    @Override
    public void init() {
        // do nothing
    }

    @Override
    public AbstractModule createModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers) {
        return new ModuleAdaptor(this, classLoader, resource, headers);
    }

    @Override
    public ModuleEntriesProvider getModuleEntriesProvider(Module module) {
        return new OSGiModuleEntriesProvider(module);
    }

    @Override
    protected void uninstallModule(Module module) {
        super.uninstallModule(module);
    }

    @Override
    public Module installModule(Bundle bundle) throws ModuleException {
        Module module = getModule(bundle.getBundleId());
        if (module == null) {
            BundleWiring wiring = bundle.adapt(BundleWiring.class);
            ClassLoader classLoader = wiring != null ? wiring.getClassLoader() : null;
            if (classLoader == null)
                throw new ModuleException("Bundle has no class loader: " + bundle);
            if (bundle.getBundleId() == 0)
                throw new ModuleException("Cannot install system bundle: " + bundle);

            module = installModule(classLoader, bundle.getHeaders());
        }
        return module;
    }

    private class OSGiModuleEntriesProvider implements ModuleEntriesProvider {

        private final Module module;

        OSGiModuleEntriesProvider(Module module) {
            this.module = module;
        }

        @Override
        public Enumeration<String> getEntryPaths(String path) {
            Bundle bundle = bundleContext.getBundle(module.getModuleId());
            return bundle.getEntryPaths(path);
        }

        @Override
        public URL getEntry(String path) {
            Bundle bundle = bundleContext.getBundle(module.getModuleId());
            return bundle.getEntry(path);
        }

        @Override
        public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
            Bundle bundle = bundleContext.getBundle(module.getModuleId());
            return bundle.findEntries(path, filePattern, recurse);
        }

    }
}
