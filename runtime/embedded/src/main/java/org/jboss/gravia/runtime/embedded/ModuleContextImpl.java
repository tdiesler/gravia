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

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
final class ModuleContextImpl implements ModuleContext {

    private final AtomicBoolean destroyed = new AtomicBoolean();
    private final Module module;

    ModuleContextImpl(Module module) {
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
        assertNotDestroyed();
        getRuntimeEvents().addModuleListener(module, listener);
    }

    @Override
    public void removeModuleListener(ModuleListener listener) {
        assertNotDestroyed();
        getRuntimeEvents().removeModuleListener(module, listener);
    }

    @Override
    public void addServiceListener(ServiceListener listener, String filterstr) {
        assertNotDestroyed();
        getRuntimeEvents().addServiceListener(module, listener, filterstr);
    }

    @Override
    public void addServiceListener(ServiceListener listener) {
        assertNotDestroyed();
        getRuntimeEvents().addServiceListener(module, listener, null);
    }

    @Override
    public void removeServiceListener(ServiceListener listener) {
        assertNotDestroyed();
        getRuntimeEvents().removeServiceListener(module, listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
        assertNotDestroyed();
        return getServiceManager().registerService(module, new String[]{ clazz.getName() }, service, properties);
    }

    @Override
    public ServiceRegistration<?> registerService(String className, Object service, Dictionary<String, ?> properties) {
        assertNotDestroyed();
        return getServiceManager().registerService(module, new String[]{ className }, service, properties);
    }

    @Override
    public ServiceRegistration<?> registerService(String[] classNames, Object service, Dictionary<String, ?> properties) {
        assertNotDestroyed();
        return getServiceManager().registerService(module, classNames, service, properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
        assertNotDestroyed();
        return (ServiceReference<S>) getServiceManager().getServiceReference(module, clazz.getName());
    }


    @Override
    public ServiceReference<?> getServiceReference(String clazzName) {
        assertNotDestroyed();
        return getServiceManager().getServiceReference(module, clazzName);
    }

    @Override
    public ServiceReference<?>[] getServiceReferences(String className, String filter) {
        assertNotDestroyed();
        List<ServiceState<?>> srefs = getServiceManager().getServiceReferences(module, className, filter, true);
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
        assertNotDestroyed();
        String className = clazz != null ? clazz.getName() : null;
        List<ServiceState<?>> srefs = getServiceManager().getServiceReferences(module, className, filter, true);

        List<ServiceReference<S>> result = new ArrayList<ServiceReference<S>>();
        for (ServiceState<?> serviceState : srefs)
            result.add((ServiceReference<S>) serviceState.getReference());

        return Collections.unmodifiableList(result);
    }

    @Override
    public ServiceReference<?>[] getAllServiceReferences(String className, String filter) {
        assertNotDestroyed();
        List<ServiceState<?>> srefs = getServiceManager().getServiceReferences(module, className, filter, false);
        if (srefs.isEmpty())
            return null;

        List<ServiceReference<?>> result = new ArrayList<ServiceReference<?>>();
        for (ServiceState<?> serviceState : srefs)
            result.add(serviceState.getReference());

        return result.toArray(new ServiceReference[result.size()]);
    }

    @Override
    public boolean ungetService(ServiceReference<?> reference) {
        assertNotDestroyed();
        ServiceState<?> serviceState = ServiceState.assertServiceState(reference);
        return getServiceManager().ungetService(module, serviceState);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> S getService(ServiceReference<S> reference) {
        assertNotDestroyed();
        ServiceState<S> serviceState = ServiceState.assertServiceState(reference);
        return getServiceManager().getService(module, serviceState);
    }

    private EmbeddedRuntimeServicesHandler getServiceManager() {
        Runtime runtime = module.adapt(Runtime.class);
        return runtime.adapt(EmbeddedRuntimeServicesHandler.class);
    }

    private EmbeddedRuntimeEventsHandler getRuntimeEvents() {
        Runtime runtime = module.adapt(Runtime.class);
        return runtime.adapt(EmbeddedRuntimeEventsHandler.class);
    }

    void assertNotDestroyed() {
        if (destroyed.get())
            throw new IllegalStateException("Invalid ModuleContext for: " + module);
    }
}
