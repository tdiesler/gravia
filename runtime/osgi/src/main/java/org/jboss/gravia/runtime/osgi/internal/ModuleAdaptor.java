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

import java.io.File;
import java.util.Dictionary;

import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleReference;

/**
 * A module adaptor.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
final class ModuleAdaptor extends AbstractModule {

    private final Bundle bundle;

    ModuleAdaptor(OSGiRuntime runtime, ClassLoader classLoader, Resource resource, Dictionary<String, String> headers) {
        super(runtime, classLoader, resource, headers);
        if (classLoader instanceof BundleReference) {
            bundle = ((BundleReference) classLoader).getBundle();
        } else {
            bundle = runtime.getSystemContext().getBundle();
        }
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
            case Bundle.UNINSTALLED:
                return State.UNINSTALLED;
            case Bundle.RESOLVED:
                return State.INSTALLED;
            case Bundle.STARTING:
                return State.STARTING;
            case Bundle.ACTIVE:
                return State.ACTIVE;
            case Bundle.STOPPING:
                return State.STOPPING;
        }
        throw new IllegalArgumentException("Unsupported bundle state: " + bundleState);
    }

    @Override
    protected Bundle getBundleAdaptor(Module module) {
        return bundle;
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
        LOGGER.info("Started: {}", this);
    }

    @Override
    public void stop() throws ModuleException {
        try {
            bundle.stop();
        } catch (BundleException ex) {
            throw ModuleException.launderThrowable(ex);
        }
        LOGGER.info("Stopped: {}", this);
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
