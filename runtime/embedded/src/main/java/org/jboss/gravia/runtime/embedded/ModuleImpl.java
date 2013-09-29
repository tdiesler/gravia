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
package org.jboss.gravia.runtime.embedded;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.AttachmentKey;
import org.jboss.gravia.resource.spi.AttachableSupport;
import org.jboss.gravia.runtime.Constants;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleActivator;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.embedded.osgi.BundleLifecycleHandler;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
final class ModuleImpl implements Module, Attachable {

    private static AttachmentKey<ModuleActivator> MODULE_ACTIVATOR_KEY = AttachmentKey.create(ModuleActivator.class);
    private static final AtomicLong moduleIdGenerator = new AtomicLong();

    private final EmbeddedRuntime runtime;
    private final ClassLoader classLoader;
    private final Map<String, Object> properties;
    private final AtomicReference<State> stateRef = new AtomicReference<State>();
    private final AtomicReference<ModuleContext> contextRef = new AtomicReference<ModuleContext>();
    private final Attachable attachments = new AttachableSupport();
    private final long moduleId;

    ModuleImpl(EmbeddedRuntime runtime, ClassLoader classLoader, Map<String, Object> props) {
        this.runtime = runtime;
        this.classLoader = classLoader;
        Map<String, Object> auxprops = new ConcurrentHashMap<String, Object>();
        if (props != null) {
            auxprops.putAll(props);
        }
        this.properties = Collections.unmodifiableMap(auxprops);
        this.moduleId = moduleIdGenerator.incrementAndGet();
        this.stateRef.set(State.UNINSTALLED);
    }

    // Module API

    @Override
    public long getModuleId() {
        return moduleId;
    }

    @Override
    public Object getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return properties.keySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A adapt(Class<A> type) {
        A result = null;
        if (type.isAssignableFrom(Runtime.class)) {
            result = (A) runtime;
        } else if (type.isAssignableFrom(ClassLoader.class)) {
            result = (A) classLoader;
        }
        return result;
    }

    @Override
    public <T> T putAttachment(AttachmentKey<T> key, T value) {
        return attachments.putAttachment(key, value);
    }

    @Override
    public <T> T getAttachment(AttachmentKey<T> key) {
        return attachments.getAttachment(key);
    }

    @Override
    public <T> T removeAttachment(AttachmentKey<T> key) {
        return attachments.removeAttachment(key);
    }

    @Override
    public State getState() {
        return stateRef.get();
    }

    void setState(State newState) {
        stateRef.set(newState);
    }

    @Override
    public ModuleContext getModuleContext() {
        return contextRef.get();
    }

    private void createModuleContext() {
        contextRef.set(new ModuleContextImpl(this));
    }

    private void destroyModuleContext() {
        ModuleContextImpl context = (ModuleContextImpl) contextRef.get();
        if (context != null) {
            context.destroy();
        }
        contextRef.set(null);
    }

    @Override
    public void start() throws ModuleException {
        assertNotUninstalled();

        setState(State.STARTING);

        // Create the {@link ModuleContext}
        createModuleContext();

        try {
            if (BundleLifecycleHandler.isInternalBundle(this)) {
                BundleLifecycleHandler.start(this);
            } else {
                String className = (String) properties.get(Constants.MODULE_ACTIVATOR);
                if (className != null) {
                    ModuleActivator moduleActivator;
                    synchronized (MODULE_ACTIVATOR_KEY) {
                        moduleActivator = attachments.getAttachment(MODULE_ACTIVATOR_KEY);
                        if (moduleActivator == null) {
                            Object result = loadClass(className).newInstance();
                            moduleActivator = (ModuleActivator) result;
                            attachments.putAttachment(MODULE_ACTIVATOR_KEY, moduleActivator);
                        }
                    }
                    if (moduleActivator != null) {
                        moduleActivator.start(getModuleContext());
                    }

                }
            }
        }

        // If the ModuleActivator is invalid or throws an exception then
        catch (Throwable th) {
            setState(State.RESOLVED);
            destroyModuleContext();
            throw new ModuleException("Cannot start module: " + this, th);
        }

        setState(State.ACTIVE);
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
        runtime.uninstallModule(this);
    }

    private void assertNotUninstalled() {
        if (stateRef.get() == State.UNINSTALLED)
            throw new IllegalStateException("Module already uninstalled: " + this);
    }
}
