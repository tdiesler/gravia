/*
 * #%L
 * JBossOSGi Framework
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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
package org.jboss.gravia.runtime.internal;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.AttachmentKey;
import org.jboss.gravia.resource.spi.AttachableSupport;
import org.jboss.gravia.runtime.Constants;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The {@link Module} implementation.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
final class ModuleImpl implements Module {

    private static AttachmentKey<BundleActivator> BUNDLE_ACTIVATOR_KEY = AttachmentKey.create(BundleActivator.class);
    private static final AtomicLong moduleIdGenerator = new AtomicLong();

    private final Runtime runtime;
    private final Type moduleType;
    private final ClassLoader classLoader;
    private final Map<String, Object> properties;
    private final AtomicReference<State> moduleState = new AtomicReference<State>();
    private final AtomicReference<ModuleContext> moduleContext = new AtomicReference<ModuleContext>();
    private final Attachable attachments = new AttachableSupport();
    private final long moduleId;

    ModuleImpl(Runtime runtime, ClassLoader classLoader, Map<String, Object> props) {
        this.runtime = runtime;
        this.classLoader = classLoader;
        Map<String, Object> auxprops = new ConcurrentHashMap<String, Object>();
        if (props != null) {
            auxprops.putAll(props);
        }
        this.properties = Collections.unmodifiableMap(auxprops);
        this.moduleType = (Type) getProperty(Constants.MODULE_TYPE, Type.OTHER);
        this.moduleId = moduleIdGenerator.incrementAndGet();
        this.moduleState.set(State.UNINSTALLED);
    }

    // Module API

    @Override
    public Runtime getRuntime() {
        return runtime;
    }

    @Override
    public long getModuleId() {
        return moduleId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A adapt(Class<A> type) {
        A result = null;
        if (type.isAssignableFrom(Bundle.class)) {
            result = (A) getBundle();
        } else if (type.isAssignableFrom(BundleContext.class)) {
            result = (A) getBundleContext();
        }
        return result;
    }

    @Override
    public State getState() {
        return moduleState.get();
    }

    void setState(State newState) {
        moduleState.set(newState);
    }

    @Override
    public ModuleContext getModuleContext() {
        return moduleContext.get();
    }

    private void createModuleContext() {
        moduleContext.set(new ModuleContextImpl(this));
    }

    private void destroyModuleContext() {
        moduleContext.set(null);
    }

    @Override
    public void start() throws ModuleException {
        assertNotUninstalled();

        setState(State.STARTING);

        // Create the {@link ModuleContext}
        createModuleContext();

        // #8 The BundleActivator.start(BundleContext) method of this bundle is called
        String className = (String) properties.get(Constants.MODULE_ACTIVATOR);
        if (className != null) {
            try {
                BundleActivator bundleActivator;
                synchronized (BUNDLE_ACTIVATOR_KEY) {
                    bundleActivator = attachments.getAttachment(BUNDLE_ACTIVATOR_KEY);
                    if (bundleActivator == null) {
                        Object result = loadClass(className).newInstance();
                        if (moduleType == Type.BUNDLE) {
                            bundleActivator = (BundleActivator) result;
                            attachments.putAttachment(BUNDLE_ACTIVATOR_KEY, bundleActivator);
                        }
                    }
                }
                if (bundleActivator != null) {
                    bundleActivator.start(getBundle().getBundleContext());
                }
            }

            // If the BundleActivator is invalid or throws an exception then
            catch (Throwable th) {
                setState(State.RESOLVED);
                destroyModuleContext();
                throw new ModuleException("Cannot start module: " + this, th);
            }
        }
        setState(State.ACTIVE);
    }

    private Bundle getBundle() {
        return new ModuleAsBundle(this);
    }

    private BundleContext getBundleContext() {
        ModuleContext context = getModuleContext();
        return context != null ? new ModuleAsBundleContext(context) : null;
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return classLoader.loadClass(className);
    }

    @Override
    public void stop() {
        assertNotUninstalled();
        setState(State.RESOLVED);
    }

    @Override
    public void uninstall() {
        assertNotUninstalled();
        setState(State.UNINSTALLED);
    }

    @Override
    public ServiceRegistration<?> registerService(String clazz, Object service, Dictionary<String, ?> properties) {
        ServiceManager serviceManager = runtime.adapt(ServiceManager.class);
        return serviceManager.registerService(this, new String[]{ clazz }, service, properties);
    }

    private Object getProperty(String key, Object defval) {
        Object value = properties.get(key);
        return value != null ? value : defval;
    }

    private void assertNotUninstalled() {
        if (moduleState.get() == State.UNINSTALLED)
            throw new IllegalStateException("Module already uninstalled: " + this);
    }
}