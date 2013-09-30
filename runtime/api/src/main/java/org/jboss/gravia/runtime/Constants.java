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

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public interface Constants {

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

    /** [TODO] */
    String OBJECTCLASS = "objectClass";

    /** [TODO] */
    String SERVICE_ID = "service.id";

    /** [TODO] */
    String SERVICE_PID = "service.pid";

    /** [TODO] */
    String SERVICE_RANKING = "service.ranking";
}
