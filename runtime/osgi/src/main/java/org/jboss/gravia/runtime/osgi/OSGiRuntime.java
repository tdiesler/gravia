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
package org.jboss.gravia.runtime.osgi;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.AbstractRuntime;
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
    private BundleTracker<Bundle> tracker;

    public OSGiRuntime(BundleContext syscontext) {
        this.syscontext = syscontext;
        RuntimeLocator.setRuntime(this);
    }

    @Override
    public void init() throws ModuleException {
        // Track installed bundles
        int states = Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING;
        tracker = new BundleTracker<Bundle>(syscontext, states, null) {
            @Override
            public Bundle addingBundle(Bundle bundle, BundleEvent event) {
                Bundle result = super.addingBundle(bundle, event);
                BundleWiring wiring = bundle.adapt(BundleWiring.class);
                ClassLoader classLoader = wiring != null ? wiring.getClassLoader() : null;
                if (bundle.getBundleId() > 0 && classLoader != null) {
                    URL url = bundle.getEntry(JarFile.MANIFEST_NAME);
                    try {
                        Manifest manifest = new Manifest(url.openStream());
                        installModule(classLoader, manifest);
                    } catch (IOException ex) {
                        LOGGER.errorf("Cannot add installed bundle");
                    }
                }
                return result;
            }
        };
    }

    @Override
    public void start() throws ModuleException {
        tracker.open();
    }

    @Override
    public void stop() throws ModuleException {
        tracker.close();
    }

    @Override
    public Object getProperty(String key) {
        return syscontext.getProperty(key);
    }

    @Override
    protected AbstractModule createModule(ClassLoader classLoader, Resource resource) {
        return new ModuleAdaptor(this, classLoader, resource);
    }

    @Override
    protected void uninstallModule(Module module) {
        super.uninstallModule(module);
    }

    public static Module mappedModule(Bundle bundle) {
        Runtime runtime = RuntimeLocator.getRuntime();
        return bundle != null ? runtime.getModule(bundle.getBundleId()) : null;
    }
}
