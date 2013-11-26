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

import static org.jboss.gravia.runtime.spi.RuntimeLogger.LOGGER;

import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;

import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.DictionaryResourceBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.AbstractRuntime;
import org.jboss.gravia.runtime.spi.ModuleEntriesProvider;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.utils.NotNullException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.util.tracker.BundleTracker;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public final class OSGiRuntime extends AbstractRuntime {

    private final BundleContext syscontext;
    private final BundleTracker<Bundle> tracker;

    public OSGiRuntime(BundleContext syscontext, PropertiesProvider propertiesProvider) {
        super(propertiesProvider);

        NotNullException.assertValue(syscontext, "syscontext");
        this.syscontext = syscontext;

        // Assert system bundle
        if (syscontext.getBundle().getBundleId() != 0)
            throw new IllegalArgumentException("Not the system bundle: " + syscontext.getBundle());

        // Install system module
        Resource resource = new DefaultResourceBuilder().addIdentityCapability(getSystemIdentity()).getResource();
        try {
            BundleWiring wiring = syscontext.getBundle().adapt(BundleWiring.class);
            installModule(wiring.getClassLoader(), resource, null, null);
        } catch (ModuleException ex) {
            throw new IllegalStateException("Cannot install system module", ex);
        }

        // Setup the bundle tracker
        tracker = new BundleTracker<Bundle>(syscontext, Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING, null) {

            @Override
            public Bundle addingBundle(Bundle bundle, BundleEvent event) {
                super.addingBundle(bundle, event);
                BundleWiring wiring = bundle.adapt(BundleWiring.class);
                ClassLoader classLoader = wiring != null ? wiring.getClassLoader() : null;
                ResourceBuilder resBuilder = new DictionaryResourceBuilder().load(bundle.getHeaders());
                if (classLoader != null && resBuilder.isValid()) {
                    try {
                        installModule(classLoader, bundle.getHeaders());
                    } catch (ModuleException ex) {
                        LOGGER.error("Cannot install module from: " + bundle, ex);
                    }
                }
                return bundle;
            }

            @Override
            public void remove(Bundle bundle) {
                Module module = getModule(bundle.getBundleId());
                if (module != null) {
                    module.uninstall();
                }
                super.remove(bundle);
            }
        };
    }

    @Override
    public void init() {
        tracker.open();
    }

    BundleContext getSystemContext() {
        return syscontext;
    }

    @Override
    public AbstractModule createModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers, Attachable context) {
        ModuleAdaptor module = new ModuleAdaptor(this, classLoader, resource, headers);
        module.putAttachment(AbstractModule.MODULE_ENTRIES_PROVIDER_KEY, new OSGiModuleEntriesProvider(module));
        return module;
    }

    @Override
    protected void uninstallModule(Module module) {
        super.uninstallModule(module);
    }

    private class OSGiModuleEntriesProvider implements ModuleEntriesProvider {

        private final Module module;

        OSGiModuleEntriesProvider(Module module) {
            this.module = module;
        }

        @Override
        public Enumeration<String> getEntryPaths(String path) {
            Bundle bundle = syscontext.getBundle(module.getModuleId());
            return bundle.getEntryPaths(path);
        }

        @Override
        public URL getEntry(String path) {
            Bundle bundle = syscontext.getBundle(module.getModuleId());
            return bundle.getEntry(path);
        }

        @Override
        public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
            Bundle bundle = syscontext.getBundle(module.getModuleId());
            return bundle.findEntries(path, filePattern, recurse);
        }

    }
}
