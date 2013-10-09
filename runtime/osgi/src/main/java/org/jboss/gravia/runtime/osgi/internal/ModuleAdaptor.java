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

import java.io.File;
import java.util.Dictionary;

import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.AbstractRuntime;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleReference;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
final class ModuleAdaptor extends AbstractModule {

    private final Bundle bundle;

    ModuleAdaptor(AbstractRuntime runtime, ClassLoader classLoader, Resource resource, Dictionary<String, String> headers) {
        super(runtime, classLoader, resource, headers);
        bundle = ((BundleReference) classLoader).getBundle();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A adapt(Class<A> type) {
        A result = super.adapt(type);
        if (result == null) {
            if (type.isAssignableFrom(Bundle.class)) {
                result = (A) bundle;
            }
        }
        return result;
    }

    @Override
    public State getState() {
        int bundleState = bundle.getState();
        switch(bundleState) {
            case Bundle.INSTALLED:
                return State.INSTALLED;
            case Bundle.RESOLVED:
                return State.RESOLVED;
            case Bundle.STARTING:
                return State.STARTING;
            case Bundle.ACTIVE:
                return State.ACTIVE;
            case Bundle.STOPPING:
                return State.STOPPING;
            case Bundle.UNINSTALLED:
                return State.UNINSTALLED;
        }
        throw new IllegalArgumentException("Unsupported bundle state: " + bundleState);
    }

    @Override
    protected void setState(State newState) {
        // ignore
    }

    @Override
    public long getModuleId() {
        return bundle.getBundleId();
    }

    @Override
    public ModuleContext getModuleContext() {
        BundleContext context = bundle.getBundleContext();
        return context != null ? new ModuleContextAdaptor(this, context) : null;
    }

    @Override
    public void start() throws ModuleException {
        try {
            bundle.start();
        } catch (BundleException ex) {
            throw ModuleException.launderThrowable(ex);
        }
    }

    @Override
    public void stop() throws ModuleException {
        try {
            bundle.stop();
        } catch (BundleException ex) {
            throw ModuleException.launderThrowable(ex);
        }
    }

    @Override
    public void uninstall() {
        try {
            bundle.uninstall();
            getRuntime().uninstallModule(this);
        } catch (BundleException ex) {
            ModuleException.launderThrowable(ex);
        }
    }

    @Override
    public File getDataFile(String filename) {
        return bundle.getDataFile(filename);
    }

    @Override
    protected OSGiRuntime getRuntime() {
        return (OSGiRuntime) super.getRuntime();
    }
}
