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
package org.jboss.gravia.runtime;

import java.io.File;
import java.util.Collection;
import java.util.Dictionary;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public interface ModuleContext {

    Module getModule();

    void addModuleListener(ModuleListener listener);

    void removeModuleListener(ModuleListener listener);

    void addServiceListener(ServiceListener listener, String filter);

    void addServiceListener(ServiceListener listener);

    void removeServiceListener(ServiceListener listener);

    <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties);

    ServiceRegistration<?> registerService(String className, Object service, Dictionary<String, ?> properties);

    ServiceRegistration<?> registerService(String[] classNames, Object service, Dictionary<String, ?> properties);

    <S> ServiceReference<S> getServiceReference(Class<S> clazz);

    ServiceReference<?> getServiceReference(String className);

    ServiceReference<?>[] getServiceReferences(String className, String filter);

    <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter);

    ServiceReference<?>[] getAllServiceReferences(String className, String filter);

    <S> S getService(ServiceReference<S> reference);

    boolean ungetService(ServiceReference<?> reference);

    File getDataFile(String filename);
}
