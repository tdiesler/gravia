/*
 * #%L
 * Gravia :: Runtime :: API
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

/**
 * Allows services to provide customized service objects.
 *
 * <p>
 * When registering a service, a {@code ServiceFactory} object can be used
 * instead of a service object, so that the module developer can gain control of
 * the specific service object granted to a module that is using the service.
 *
 * <p>
 * When this happens, the {@code ModuleContext.getService(ServiceReference)}
 * method calls the {@code ServiceFactory.getService} method to create a service
 * object specifically for the requesting module. The service object returned by
 * the {@code ServiceFactory} is cached by the Runtime until the module
 * releases its use of the service.
 *
 * <p>
 * When the module's use count for the service is decremented to zero (including
 * the module stopping or the service being unregistered), the
 * {@code ServiceFactory.ungetService} method is called.
 *
 * <p>
 * {@code ServiceFactory} objects are only used by the Runtime and are not
 * made available to other modules in the environment. The Runtime may
 * concurrently call a {@code ServiceFactory}.
 *
 * @param <S> Type of Service
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @see ModuleContext#getService(ServiceReference)
 * @ThreadSafe
 */
public interface ServiceFactory<S> {

    /**
     * Creates a new service object.
     *
     * <p>
     * The Runtime invokes this method the first time the specified
     * {@code module} requests a service object using the
     * {@code ModuleContext.getService(ServiceReference)} method. The service
     * factory can then return a specific service object for each module.
     *
     * <p>
     * The Runtime must check that the returned service object is valid. If
     * the returned service object is {@code null} or is not an
     * {@code instanceof} all the classes named when the service was registered,
     * a framework event of type {@link RuntimeEvent#ERROR} is fired
     * containing a service exception of type
     * {@link ServiceException#FACTORY_ERROR} and {@code null} is returned to
     * the module. If this method throws an exception, a framework event of type
     * {@link RuntimeEvent#ERROR} is fired containing a service exception of
     * type {@link ServiceException#FACTORY_EXCEPTION} with the thrown exception
     * as the cause and {@code null} is returned to the module. If this method
     * is recursively called for the specified module, a framework event of type
     * {@link RuntimeEvent#ERROR} is fired containing a service exception of
     * type {@link ServiceException#FACTORY_RECURSION} and {@code null} is
     * returned to the module.
     *
     * <p>
     * The Runtime caches the valid service object and will return the same
     * service object on any future call to {@code ModuleContext.getService} for
     * the specified module. This means the Runtime must not allow this method
     * to be concurrently called for the specified module.
     *
     * @param module The module requesting the service.
     * @param registration The {@code ServiceRegistration} object for the
     *        requested service.
     * @return A service object that <strong>must</strong> be an instance of all
     *         the classes named when the service was registered.
     * @see ModuleContext#getService(ServiceReference)
     */
    S getService(Module module, ServiceRegistration<S> registration);

    /**
     * Releases a service object.
     *
     * <p>
     * The Runtime invokes this method when a service has been released by a
     * module. The service object may then be destroyed.
     *
     * <p>
     * If this method throws an exception, a framework event of type
     * {@link RuntimeEvent#ERROR} is fired containing a service exception of
     * type {@link ServiceException#FACTORY_EXCEPTION} with the thrown exception
     * as the cause.
     *
     * @param module The module releasing the service.
     * @param registration The {@code ServiceRegistration} object for the
     *        service being released.
     * @param service The service object returned by a previous call to the
     *        {@link #getService(Module, ServiceRegistration) getService}
     *        method.
     * @see ModuleContext#ungetService(ServiceReference)
     */
    void ungetService(Module module, ServiceRegistration<S> registration, S service);
}
