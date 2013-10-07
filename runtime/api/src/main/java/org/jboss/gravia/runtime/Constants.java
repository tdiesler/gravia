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

import org.jboss.gravia.resource.ManifestBuilder;

/**
 * Defines standard names for the environment system properties, service
 * properties, and Manifest header attribute keys.
 * <p>
 * The values associated with these keys are of type {@code String}, unless
 * otherwise indicated.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public interface Constants {

    /**
     * Header attribute identifying the module's activator class.
     *
     * <p>
     * If present, this header specifies the name of the module resource class
     * that implements the {@code ModuleActivator} interface and whose
     * {@code start} and {@code stop} methods are called by the Runtime when
     * the module is started and stopped, respectively.
     *
     * <p>
     * The header value may be retrieved from the {@code Dictionary} object
     * returned by the {@link Module.getHeaders()} method.
     */
    String MODULE_ACTIVATOR = ManifestBuilder.MODULE_ACTIVATOR;

    /**
     * Runtime property specifying the persistent storage area used
     * by the runtime. The value of this property must be a valid file path in
     * the file system to a directory. If the specified directory does not exist
     * then the runtime will create the directory. If the specified path
     * exists but is not a directory or if the runtime fails to create the
     * storage directory, then runtime initialization must fail. The runtime
     * is free to use this directory as it sees fit. This area can not be shared
     * with anything else.
     * <p>
     * If this property is not set, the runtime should use a reasonable
     * platform default for the persistent storage area.
     */
    String RUNTIME_STORAGE = "org.jboss.gravia.runtime.storage";

    /**
     * Runtime property specifying if and when the persistent
     * storage area for the runtime should be cleaned. If this property is not
     * set, then the runtime storage area must not be cleaned.
     */
    String RUNTIME_STORAGE_CLEAN = "org.jboss.gravia.runtime.storage.clean";

    /**
     * Specifies that the runtime storage area must be cleaned before the
     * runtime is initialized for the first time. Subsequent starts of the
     * runtime will not result in cleaning the runtime storage area.
     */
    String RUNTIME_STORAGE_CLEAN_ONFIRSTINIT = "onFirstInit";

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
     * The value of this property is assigned by the Runtime when a service is
     * registered. The Runtime assigns a unique value that is larger than all
     * previously assigned values since the Runtime was started. These values
     * are NOT persistent across restarts of the Runtime.
     */
    String SERVICE_ID = "service.id";

    /**
     * Service property identifying a service's persistent identifier.
     *
     * <p>
     * This property may be supplied in the {@code properties}
     * {@code Dictionary} object passed to the
     * {@code ModuleContext.registerService} method. The value of this property
     * must be of type {@code String}, {@code String[]}, or {@code Collection}
     * of {@code String}.
     *
     * <p>
     * A service's persistent identifier uniquely identifies the service and
     * persists across multiple Runtime invocations.
     */
    String SERVICE_PID = "service.pid";

    /**
     * Service property identifying a service's ranking number.
     *
     * <p>
     * This property may be supplied in the {@code properties
     * Dictionary} object passed to the {@code ModuleContext.registerService}
     * method. The value of this property must be of type {@code Integer}.
     *
     * <p>
     * The service ranking is used by the Runtime to determine the <i>natural
     * order</i> of services, see {@link ServiceReference#compareTo(Object)},
     * and the <i>default</i> service to be returned from a call to the
     * {@link ModuleContext#getServiceReference(Class)} or
     * {@link ModuleContext#getServiceReference(String)} method.
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
    String SERVICE_RANKING = "service.ranking";
}
