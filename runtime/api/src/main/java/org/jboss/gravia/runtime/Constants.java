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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public interface Constants {

    String MODULE_ACTIVATOR = "Module-Activator";

    String MODULE_TYPE = "Module-Type";

    /**
     * Service property identifying all of the class names under which a service
     * was registered in the Runtime. The value of this property must be of
     * type {@code String[]}.
     * 
     * <p>
     * This property is set by the Runtime when a service is registered.
     */
    String OBJECTCLASS = "objectClass";

    /**
     * Service property identifying a service's registration number. The value
     * of this property must be of type {@code Long}.
     * 
     * <p>
     * The value of this property is assigned by the Framework when a service is
     * registered. The Framework assigns a unique value that is larger than all
     * previously assigned values since the Framework was started. These values
     * are NOT persistent across restarts of the Framework.
     */
    String  SERVICE_ID                              = "service.id";

    /**
     * Service property identifying a service's persistent identifier.
     * 
     * <p>
     * This property may be supplied in the {@code properties}
     * {@code Dictionary} object passed to the
     * {@code BundleContext.registerService} method. The value of this property
     * must be of type {@code String}, {@code String[]}, or {@code Collection}
     * of {@code String}.
     * 
     * <p>
     * A service's persistent identifier uniquely identifies the service and
     * persists across multiple Framework invocations.
     * 
     * <p>
     * By convention, every bundle has its own unique namespace, starting with
     * the bundle's identifier (see {@link Bundle#getBundleId()}) and followed
     * by a dot (.). A bundle may use this as the prefix of the persistent
     * identifiers for the services it registers.
     */
    String  SERVICE_PID                             = "service.pid";

    /**
     * Service property identifying a service's ranking number.
     * 
     * <p>
     * This property may be supplied in the {@code properties
     * Dictionary} object passed to the {@code BundleContext.registerService}
     * method. The value of this property must be of type {@code Integer}.
     * 
     * <p>
     * The service ranking is used by the Framework to determine the <i>natural
     * order</i> of services, see {@link ServiceReference#compareTo(Object)},
     * and the <i>default</i> service to be returned from a call to the
     * {@link BundleContext#getServiceReference(Class)} or
     * {@link BundleContext#getServiceReference(String)} method.
     * 
     * <p>
     * The default ranking is zero (0). A service with a ranking of
     * {@code Integer.MAX_VALUE} is very likely to be returned as the default
     * service, whereas a service with a ranking of {@code Integer.MIN_VALUE} is
     * very unlikely to be returned.
     * 
     * <p>
     * If the supplied property value is not of type {@code Integer}, it is
     * deemed to have a ranking value of zero.
     */
    String  SERVICE_RANKING                         = "service.ranking";
}
