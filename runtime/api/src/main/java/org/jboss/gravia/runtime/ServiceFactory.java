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
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public interface ServiceFactory<S> {
    /**
     * Creates a new service object.
     * 
     * <p>
     * The Framework invokes this method the first time the specified
     * {@code bundle} requests a service object using the
     * {@code BundleContext.getService(ServiceReference)} method. The service
     * factory can then return a specific service object for each bundle.
     * 
     * <p>
     * The Framework must check that the returned service object is valid. If
     * the returned service object is {@code null} or is not an
     * {@code instanceof} all the classes named when the service was registered,
     * a framework event of type {@link FrameworkEvent#ERROR} is fired
     * containing a service exception of type
     * {@link ServiceException#FACTORY_ERROR} and {@code null} is returned to
     * the bundle. If this method throws an exception, a framework event of type
     * {@link FrameworkEvent#ERROR} is fired containing a service exception of
     * type {@link ServiceException#FACTORY_EXCEPTION} with the thrown exception
     * as the cause and {@code null} is returned to the bundle. If this method
     * is recursively called for the specified bundle, a framework event of type
     * {@link FrameworkEvent#ERROR} is fired containing a service exception of
     * type {@link ServiceException#FACTORY_RECURSION} and {@code null} is
     * returned to the bundle.
     * 
     * <p>
     * The Framework caches the valid service object and will return the same
     * service object on any future call to {@code BundleContext.getService} for
     * the specified bundle. This means the Framework must not allow this method
     * to be concurrently called for the specified bundle.
     * 
     * @param bundle The bundle requesting the service.
     * @param registration The {@code ServiceRegistration} object for the
     *        requested service.
     * @return A service object that <strong>must</strong> be an instance of all
     *         the classes named when the service was registered.
     * @see BundleContext#getService(ServiceReference)
     */
    public S getService(Module module, ServiceRegistration<S> registration);

    /**
     * Releases a service object.
     * 
     * <p>
     * The Framework invokes this method when a service has been released by a
     * bundle. The service object may then be destroyed.
     * 
     * <p>
     * If this method throws an exception, a framework event of type
     * {@link FrameworkEvent#ERROR} is fired containing a service exception of
     * type {@link ServiceException#FACTORY_EXCEPTION} with the thrown exception
     * as the cause.
     * 
     * @param bundle The bundle releasing the service.
     * @param registration The {@code ServiceRegistration} object for the
     *        service being released.
     * @param service The service object returned by a previous call to the
     *        {@link #getService(Bundle, ServiceRegistration) getService}
     *        method.
     * @see BundleContext#ungetService(ServiceReference)
     */
    public void ungetService(Module module, ServiceRegistration<S> registration, S service);
}
