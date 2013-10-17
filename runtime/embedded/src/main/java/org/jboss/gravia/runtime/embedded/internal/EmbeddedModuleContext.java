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
package org.jboss.gravia.runtime.embedded.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import org.jboss.gravia.resource.spi.NotNullException;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleListener;
import org.jboss.gravia.runtime.ServiceListener;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.AbstractModuleContext;
import org.jboss.gravia.runtime.spi.AbstractRuntime;
import org.jboss.gravia.runtime.spi.RuntimeEventsManager;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
final class EmbeddedModuleContext extends AbstractModuleContext {

    EmbeddedModuleContext(Module module) {
        super(module);
    }

    @Override
    protected void destroy() {
        super.destroy();
    }

    @Override
    public void addModuleListener(ModuleListener listener) {
        NotNullException.assertValue(listener, "listener");
        assertNotDestroyed();
        getEventsHandler().addModuleListener(getModule(), listener);
    }

    @Override
    public void removeModuleListener(ModuleListener listener) {
        NotNullException.assertValue(listener, "listener");
        assertNotDestroyed();
        getEventsHandler().removeModuleListener(getModule(), listener);
    }

    @Override
    public void addServiceListener(ServiceListener listener, String filterstr) {
        NotNullException.assertValue(listener, "listener");
        assertNotDestroyed();
        getEventsHandler().addServiceListener(getModule(), listener, filterstr);
    }

    @Override
    public void addServiceListener(ServiceListener listener) {
        NotNullException.assertValue(listener, "listener");
        assertNotDestroyed();
        getEventsHandler().addServiceListener(getModule(), listener, null);
    }

    @Override
    public void removeServiceListener(ServiceListener listener) {
        NotNullException.assertValue(listener, "listener");
        assertNotDestroyed();
        getEventsHandler().removeServiceListener(getModule(), listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
        NotNullException.assertValue(clazz, "clazz");
        NotNullException.assertValue(service, "service");
        assertNotDestroyed();
        return getServicesHandler().registerService(this, new String[]{ clazz.getName() }, service, properties);
    }

    @Override
    public ServiceRegistration<?> registerService(String className, Object service, Dictionary<String, ?> properties) {
        NotNullException.assertValue(className, "className");
        NotNullException.assertValue(service, "service");
        assertNotDestroyed();
        return getServicesHandler().registerService(this, new String[]{ className }, service, properties);
    }

    @Override
    public ServiceRegistration<?> registerService(String[] classNames, Object service, Dictionary<String, ?> properties) {
        NotNullException.assertValue(classNames, "classNames");
        NotNullException.assertValue(service, "service");
        assertNotDestroyed();
        return getServicesHandler().registerService(this, classNames, service, properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
        NotNullException.assertValue(clazz, "clazz");

        assertNotDestroyed();
        return (ServiceReference<S>) getServicesHandler().getServiceReference(this, clazz.getName());
    }


    @Override
    public ServiceReference<?> getServiceReference(String className) {
        NotNullException.assertValue(className, "className");

        assertNotDestroyed();
        return getServicesHandler().getServiceReference(this, className);
    }

    @Override
    public ServiceReference<?>[] getServiceReferences(String className, String filter) {
        NotNullException.assertValue(className, "className");

        assertNotDestroyed();
        List<ServiceState<?>> srefs = getServicesHandler().getServiceReferences(this, className, filter, true);
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
        NotNullException.assertValue(clazz, "clazz");

        assertNotDestroyed();
        String className = clazz != null ? clazz.getName() : null;
        List<ServiceState<?>> srefs = getServicesHandler().getServiceReferences(this, className, filter, true);

        List<ServiceReference<S>> result = new ArrayList<ServiceReference<S>>();
        for (ServiceState<?> serviceState : srefs)
            result.add((ServiceReference<S>) serviceState.getReference());

        return Collections.unmodifiableList(result);
    }

    @Override
    public ServiceReference<?>[] getAllServiceReferences(String className, String filter) {
        NotNullException.assertValue(className, "className");

        assertNotDestroyed();
        List<ServiceState<?>> srefs = getServicesHandler().getServiceReferences(this, className, filter, false);
        if (srefs.isEmpty())
            return null;

        List<ServiceReference<?>> result = new ArrayList<ServiceReference<?>>();
        for (ServiceState<?> serviceState : srefs)
            result.add(serviceState.getReference());

        return result.toArray(new ServiceReference[result.size()]);
    }

    @Override
    public boolean ungetService(ServiceReference<?> reference) {
        NotNullException.assertValue(reference, "reference");

        assertNotDestroyed();
        ServiceState<?> serviceState = ServiceState.assertServiceState(reference);
        return getServicesHandler().ungetService((AbstractModule) getModule(), serviceState);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> S getService(ServiceReference<S> reference) {
        NotNullException.assertValue(reference, "reference");

        assertNotDestroyed();
        ServiceState<S> serviceState = ServiceState.assertServiceState(reference);
        return getServicesHandler().getService(this, serviceState);
    }

    private RuntimeServicesManager getServicesHandler() {
        AbstractRuntime runtime = getModule().adapt(AbstractRuntime.class);
        return runtime.adapt(RuntimeServicesManager.class);
    }

    private RuntimeEventsManager getEventsHandler() {
        AbstractRuntime runtime = getModule().adapt(AbstractRuntime.class);
        return runtime.adapt(RuntimeEventsManager.class);
    }
}
