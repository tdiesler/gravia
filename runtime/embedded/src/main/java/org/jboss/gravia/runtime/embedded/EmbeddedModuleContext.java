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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleListener;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.ServiceListener;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.gravia.runtime.spi.RuntimeEventsHandler;
import org.jboss.gravia.runtime.spi.RuntimeStorageHandler;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
final class EmbeddedModuleContext implements ModuleContext {

    private final AtomicBoolean destroyed = new AtomicBoolean();
    private final Module module;

    EmbeddedModuleContext(Module module) {
        this.module = module;
    }

    void destroy() {
        destroyed.set(true);
    }

    // ModuleContext API

    @Override
    public Module getModule() {
        return module;
    }

    @Override
    public void addModuleListener(ModuleListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("Null listener");

        assertNotDestroyed();
        getEventsHandler().addModuleListener(module, listener);
    }

    @Override
    public void removeModuleListener(ModuleListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("Null listener");

        assertNotDestroyed();
        getEventsHandler().removeModuleListener(module, listener);
    }

    @Override
    public void addServiceListener(ServiceListener listener, String filterstr) {
        if (listener == null)
            throw new IllegalArgumentException("Null listener");

        assertNotDestroyed();
        getEventsHandler().addServiceListener(module, listener, filterstr);
    }

    @Override
    public void addServiceListener(ServiceListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("Null listener");

        assertNotDestroyed();
        getEventsHandler().addServiceListener(module, listener, null);
    }

    @Override
    public void removeServiceListener(ServiceListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("Null listener");

        assertNotDestroyed();
        getEventsHandler().removeServiceListener(module, listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
        if (clazz == null)
            throw new IllegalArgumentException("Null clazz");
        if (service == null)
            throw new IllegalArgumentException("Null service");

        assertNotDestroyed();
        return getServicesHandler().registerService(module, new String[]{ clazz.getName() }, service, properties);
    }

    @Override
    public ServiceRegistration<?> registerService(String className, Object service, Dictionary<String, ?> properties) {
        if (className == null)
            throw new IllegalArgumentException("Null className");
        if (service == null)
            throw new IllegalArgumentException("Null service");

        assertNotDestroyed();
        return getServicesHandler().registerService(module, new String[]{ className }, service, properties);
    }

    @Override
    public ServiceRegistration<?> registerService(String[] classNames, Object service, Dictionary<String, ?> properties) {
        if (classNames == null)
            throw new IllegalArgumentException("Null classNames");
        if (service == null)
            throw new IllegalArgumentException("Null service");

        assertNotDestroyed();
        return getServicesHandler().registerService(module, classNames, service, properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
        if (clazz == null)
            throw new IllegalArgumentException("Null clazz");

        assertNotDestroyed();
        return (ServiceReference<S>) getServicesHandler().getServiceReference(module, clazz.getName());
    }


    @Override
    public ServiceReference<?> getServiceReference(String className) {
        if (className == null)
            throw new IllegalArgumentException("Null className");

        assertNotDestroyed();
        return getServicesHandler().getServiceReference(module, className);
    }

    @Override
    public ServiceReference<?>[] getServiceReferences(String className, String filter) {
        if (className == null)
            throw new IllegalArgumentException("Null className");

        assertNotDestroyed();
        List<ServiceState<?>> srefs = getServicesHandler().getServiceReferences(module, className, filter, true);
        if (srefs.isEmpty())
            return null;

        List<ServiceReference<?>> result = new ArrayList<ServiceReference<?>>();
        for (ServiceState<?> serviceState : srefs)
            result.add(serviceState.getReference());

        return result.toArray(new ServiceReference[result.size()]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter) {
        if (clazz == null)
            throw new IllegalArgumentException("Null clazz");

        assertNotDestroyed();
        String className = clazz != null ? clazz.getName() : null;
        List<ServiceState<?>> srefs = getServicesHandler().getServiceReferences(module, className, filter, true);

        List<ServiceReference<S>> result = new ArrayList<ServiceReference<S>>();
        for (ServiceState<?> serviceState : srefs)
            result.add((ServiceReference<S>) serviceState.getReference());

        return Collections.unmodifiableList(result);
    }

    @Override
    public ServiceReference<?>[] getAllServiceReferences(String className, String filter) {
        if (className == null)
            throw new IllegalArgumentException("Null className");

        assertNotDestroyed();
        List<ServiceState<?>> srefs = getServicesHandler().getServiceReferences(module, className, filter, false);
        if (srefs.isEmpty())
            return null;

        List<ServiceReference<?>> result = new ArrayList<ServiceReference<?>>();
        for (ServiceState<?> serviceState : srefs)
            result.add(serviceState.getReference());

        return result.toArray(new ServiceReference[result.size()]);
    }

    @Override
    public boolean ungetService(ServiceReference<?> reference) {
        if (reference == null)
            throw new IllegalArgumentException("Null reference");

        assertNotDestroyed();
        ServiceState<?> serviceState = ServiceState.assertServiceState(reference);
        return getServicesHandler().ungetService(module, serviceState);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> S getService(ServiceReference<S> reference) {
        if (reference == null)
            throw new IllegalArgumentException("Null reference");

        assertNotDestroyed();
        ServiceState<S> serviceState = ServiceState.assertServiceState(reference);
        return getServicesHandler().getService(module, serviceState);
    }

    @Override
    public File getDataFile(String filename) {
        RuntimeStorageHandler storageHandler = getStorageHandler();
        return storageHandler.getDataFile(module, filename);
    }

    private RuntimeServicesHandler getServicesHandler() {
        Runtime runtime = module.adapt(Runtime.class);
        return runtime.adapt(RuntimeServicesHandler.class);
    }

    private RuntimeEventsHandler getEventsHandler() {
        Runtime runtime = module.adapt(Runtime.class);
        return runtime.adapt(RuntimeEventsHandler.class);
    }

    private RuntimeStorageHandler getStorageHandler() {
        Runtime runtime = module.adapt(Runtime.class);
        return runtime.adapt(RuntimeStorageHandler.class);
    }

    void assertNotDestroyed() {
        if (destroyed.get())
            throw new IllegalStateException("Invalid ModuleContext for: " + module);
    }
}
