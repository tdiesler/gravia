/*
 * #%L
 * Gravia :: Runtime :: Embedded
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
package org.jboss.gravia.runtime.embedded.internal;

import static org.jboss.gravia.runtime.spi.RuntimeLogger.LOGGER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleListener;
import org.jboss.gravia.runtime.ServiceListener;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.AbstractModuleContext;
import org.jboss.gravia.runtime.spi.AbstractRuntime;
import org.jboss.gravia.runtime.spi.RuntimeEventsManager;
import org.jboss.gravia.utils.IllegalArgumentAssertion;

/**
 * The embedded {@link ModuleContext}
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
        IllegalArgumentAssertion.assertNotNull(listener, "listener");
        assertNotDestroyed();
        getEventsManager().addModuleListener(getModule(), listener);
    }

    @Override
    public void removeModuleListener(ModuleListener listener) {
        IllegalArgumentAssertion.assertNotNull(listener, "listener");
        assertNotDestroyed();
        getEventsManager().removeModuleListener(getModule(), listener);
    }

    @Override
    public void addServiceListener(ServiceListener listener, String filterstr) {
        IllegalArgumentAssertion.assertNotNull(listener, "listener");
        assertNotDestroyed();
        getEventsManager().addServiceListener(getModule(), listener, filterstr);
    }

    @Override
    public void addServiceListener(ServiceListener listener) {
        IllegalArgumentAssertion.assertNotNull(listener, "listener");
        assertNotDestroyed();
        getEventsManager().addServiceListener(getModule(), listener, null);
    }

    @Override
    public void removeServiceListener(ServiceListener listener) {
        IllegalArgumentAssertion.assertNotNull(listener, "listener");
        assertNotDestroyed();
        getEventsManager().removeServiceListener(getModule(), listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
        IllegalArgumentAssertion.assertNotNull(clazz, "clazz");
        IllegalArgumentAssertion.assertNotNull(service, "service");
        assertNotDestroyed();
        return getServicesManager().registerService(this, new String[]{ clazz.getName() }, service, properties);
    }

    @Override
    public ServiceRegistration<?> registerService(String className, Object service, Dictionary<String, ?> properties) {
        IllegalArgumentAssertion.assertNotNull(className, "className");
        IllegalArgumentAssertion.assertNotNull(service, "service");
        assertNotDestroyed();
        return getServicesManager().registerService(this, new String[]{ className }, service, properties);
    }

    @Override
    public ServiceRegistration<?> registerService(String[] classNames, Object service, Dictionary<String, ?> properties) {
        IllegalArgumentAssertion.assertNotNull(classNames, "classNames");
        IllegalArgumentAssertion.assertNotNull(service, "service");
        assertNotDestroyed();
        return getServicesManager().registerService(this, classNames, service, properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
        IllegalArgumentAssertion.assertNotNull(clazz, "clazz");
        assertNotDestroyed();
        return (ServiceReference<S>) getServicesManager().getServiceReference(this, clazz.getName());
    }


    @Override
    public ServiceReference<?> getServiceReference(String className) {
        IllegalArgumentAssertion.assertNotNull(className, "className");
        assertNotDestroyed();
        return getServicesManager().getServiceReference(this, className);
    }

    @Override
    public ServiceReference<?>[] getServiceReferences(String className, String filter) {
        assertNotDestroyed();

        List<ServiceState<?>> srefs = getServicesManager().getServiceReferences(this, className, filter, true);
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
        List<ServiceState<?>> srefs = getServicesManager().getServiceReferences(this, className, filter, true);

        List<ServiceReference<S>> result = new ArrayList<ServiceReference<S>>();
        for (ServiceState<?> serviceState : srefs)
            result.add((ServiceReference<S>) serviceState.getReference());

        return Collections.unmodifiableList(result);
    }

    @Override
    public ServiceReference<?>[] getAllServiceReferences(String className, String filter) {
        assertNotDestroyed();

        List<ServiceState<?>> srefs = getServicesManager().getServiceReferences(this, className, filter, false);
        if (srefs.isEmpty())
            return null;

        List<ServiceReference<?>> result = new ArrayList<ServiceReference<?>>();
        for (ServiceState<?> serviceState : srefs)
            result.add(serviceState.getReference());

        return result.toArray(new ServiceReference[result.size()]);
    }

    @Override
    public boolean ungetService(ServiceReference<?> reference) {
        IllegalArgumentAssertion.assertNotNull(reference, "reference");
        if (isDestroyed()) {
            LOGGER.warn("Cannot ungetService " + reference + " from already destroyed module context: " + this);
            return false;
        } else {
            ServiceState<?> serviceState = ServiceState.assertServiceState(reference);
            return getServicesManager().ungetService((AbstractModule) getModule(), serviceState);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> S getService(ServiceReference<S> reference) {
        IllegalArgumentAssertion.assertNotNull(reference, "reference");
        assertNotDestroyed();

        ServiceState<S> serviceState = ServiceState.assertServiceState(reference);
        return getServicesManager().getService(this, serviceState);
    }

    private RuntimeServicesManager getServicesManager() {
        AbstractRuntime runtime = getModule().adapt(AbstractRuntime.class);
        return runtime.adapt(RuntimeServicesManager.class);
    }

    private RuntimeEventsManager getEventsManager() {
        AbstractRuntime runtime = getModule().adapt(AbstractRuntime.class);
        return runtime.adapt(RuntimeEventsManager.class);
    }
}
