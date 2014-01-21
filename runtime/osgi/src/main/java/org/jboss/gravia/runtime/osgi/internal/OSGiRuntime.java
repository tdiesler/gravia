/*
 * #%L
 * Gravia :: Runtime :: OSGi
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
package org.jboss.gravia.runtime.osgi.internal;

import static org.jboss.gravia.runtime.spi.RuntimeLogger.LOGGER;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;

import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.DictionaryResourceBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
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
 * The OSGi {@link Runtime}
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
    protected AbstractModule createModule(ClassLoader classLoader, Resource resource, Dictionary<String, String> headers, Attachable context) {
        return new ModuleAdaptor(this, classLoader, resource, headers);
    }

    @Override
    protected ModuleEntriesProvider getDefaultEntriesProvider(Module module, Attachable context) {
        return new OSGiModuleEntriesProvider(module);
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
        public List<String> getEntryPaths(String path) {
            Bundle bundle = syscontext.getBundle(module.getModuleId());
            Enumeration<String> paths = bundle.getEntryPaths(path);
            List<String> result = new ArrayList<String>();
            if (paths != null) {
                while (paths.hasMoreElements()) {
                    String element = paths.nextElement();
                    result.add(element);
                }
            }
            return Collections.unmodifiableList(result);
        }

        @Override
        public URL getEntry(String path) {
            Bundle bundle = syscontext.getBundle(module.getModuleId());
            return bundle.getEntry(path);
        }

        @Override
        public List<URL> findEntries(String path, String filePattern, boolean recurse) {
            Bundle bundle = syscontext.getBundle(module.getModuleId());
            Enumeration<URL> paths = bundle.findEntries(path, filePattern, recurse);
            List<URL> result = new ArrayList<URL>();
            if (paths != null) {
                while (paths.hasMoreElements()) {
                    URL element = paths.nextElement();
                    result.add(element);
                }
            }
            return Collections.unmodifiableList(result);
        }
    }
}
