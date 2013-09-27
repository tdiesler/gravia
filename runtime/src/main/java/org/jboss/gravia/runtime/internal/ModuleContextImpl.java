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

import java.util.Dictionary;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * The {@link Module} implementation.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
final class ModuleContextImpl implements ModuleContext {

    private final Module module;

    ModuleContextImpl(Module module) {
        this.module = module;
    }

    // ModuleContext API

    @Override
    public Module getModule() {
        return module;
    }

    @Override
    public ServiceRegistration<?> registerService(String clazz, Object service, Dictionary<String, ?> properties) {
        return getServiceManager().registerService(module, new String[]{ clazz }, service, properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
        return (ServiceReference<S>) getServiceManager().getServiceReference(module, clazz.getName());
    }

    @Override
    public <S> S getService(ServiceReference<S> reference) {
        return getServiceManager().getService(module, (ServiceState<S>) reference);
    }

    private ServiceManager getServiceManager() {
        return module.getRuntime().adapt(ServiceManager.class);
    }
}