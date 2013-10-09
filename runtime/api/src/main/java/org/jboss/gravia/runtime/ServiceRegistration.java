/*
 * #%L
 * JBossOSGi Runtime
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

import java.util.Dictionary;


/**
 * A registered service.
 *
 * <p>
 * The Runtime returns a {@code ServiceRegistration} object when a
 * {@code ModuleContext.registerService} method invocation is successful. The
 * {@code ServiceRegistration} object is for the private use of the registering
 * module and should not be shared with other modules.
 * <p>
 * The {@code ServiceRegistration} object may be used to update the properties
 * of the service or to unregister the service.
 *
 * @param <T> Type of Service.
 *
 * @see ModuleContext#registerService(String[],Object,Dictionary)
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @ThreadSafe
 */
public interface ServiceRegistration<T> {

    /**
     * Returns a {@code ServiceReference} object for a service being registered.
     * <p>
     * The {@code ServiceReference} object may be shared with other modules.
     *
     * @throws IllegalStateException If this {@code ServiceRegistration} object
     *         has already been unregistered.
     * @return {@code ServiceReference} object.
     */
    ServiceReference<T> getReference();

    /**
     * Updates the properties associated with a service.
     *
     * <p>
     * The {@link Constants#OBJECTCLASS} and {@link Constants#SERVICE_ID} keys
     * cannot be modified by this method. These values are set by the Runtime
     * when the service is registered.
     *
     * <p>
     * The following steps are required to modify service properties:
     * <ol>
     * <li>The service's properties are replaced with the provided properties.
     * <li>A service event of type {@link ServiceEvent#MODIFIED} is fired.
     * </ol>
     *
     * @param properties The properties for this service. See {@link Constants}
     *        for a list of standard service property keys. Changes should not
     *        be made to this object after calling this method. To update the
     *        service's properties this method should be called again.
     *
     * @throws IllegalStateException If this {@code ServiceRegistration} object
     *         has already been unregistered.
     * @throws IllegalArgumentException If {@code properties} contains case
     *         variants of the same key name.
     */
    void setProperties(Dictionary<String, ?> properties);

    /**
     * Unregisters a service. Remove a {@code ServiceRegistration} object from
     * the Runtime service registry. All {@code ServiceReference} objects
     * associated with this {@code ServiceRegistration} object can no longer be
     * used to interact with the service once unregistration is complete.
     *
     * <p>
     * The following steps are required to unregister a service:
     * <ol>
     * <li>The service is removed from the Runtime service registry so that it
     * can no longer be obtained.
     * <li>A service event of type {@link ServiceEvent#UNREGISTERING} is fired
     * so that modules using this service can release their use of the service.
     * Once delivery of the service event is complete, the
     * {@code ServiceReference} objects for the service may no longer be used to
     * get a service object for the service.
     * <li>For each module whose use count for this service is greater than
     * zero: <br>
     * The module's use count for this service is set to zero. <br>
     * If the service was registered with a {@link ServiceFactory} object, the
     * {@code ServiceFactory.ungetService} method is called to release the
     * service object for the module.
     * </ol>
     *
     * @throws IllegalStateException If this {@code ServiceRegistration} object
     *         has already been unregistered.
     * @see ModuleContext#ungetService(ServiceReference)
     * @see ServiceFactory#ungetService(Module, ServiceRegistration, Object)
     */
    void unregister();
}
