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
package org.jboss.gravia.runtime.internal;

import java.util.Dictionary;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
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
    @SuppressWarnings("unchecked")
    public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
        assertNotDestroyed();
        return (ServiceReference<S>) getServiceManager().getServiceReference(module, clazz.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> S getService(ServiceReference<S> reference) {
        assertNotDestroyed();
        ServiceState<S> serviceState = ServiceState.assertServiceState(reference);
        return getServiceManager().getService(module, serviceState);
    }

    private EmbeddedRuntimeServicesHandler getServiceManager() {
        return module.getRuntime().adapt(EmbeddedRuntimeServicesHandler.class);
    }

    private EmbeddedRuntimeEventsHandler getRuntimeEvents() {
        return module.getRuntime().adapt(EmbeddedRuntimeEventsHandler.class);
    }

    void assertNotDestroyed() {
        if (destroyed.get())
            throw new IllegalStateException("Invalid ModuleContext for: " + module);
    }
}
