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

import java.util.EventListener;


/**
/**
 * A {@code ServiceEvent} listener. {@code ServiceListener} is a listener
 * interface that may be implemented by a module developer. When a
 * {@code ServiceEvent} is fired, it is synchronously delivered to a
 * {@code ServiceListener}. The Runtime may deliver {@code ServiceEvent}
 * objects to a {@code ServiceListener} out of order and may concurrently call
 * and/or reenter a {@code ServiceListener}.
 *
 * <p>
 * A {@code ServiceListener} object is registered with the Runtime using the
 * {@code ModuleContext.addServiceListener} method. {@code ServiceListener}
 * objects are called with a {@code ServiceEvent} object when a service is
 * registered, modified, or is in the process of unregistering.
 *
 * <p>
 * {@code ServiceEvent} object delivery to {@code ServiceListener} objects is
 * filtered by the filter specified when the listener was registered.
 *
 * <p>
 * {@code ServiceEvent} object delivery to {@code ServiceListener} objects is
 * filtered according to package sources as defined in
 * {@link ServiceReference#isAssignableTo(Module, String)}.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @see ServiceEvent
 *
 * @ThreadSafe
 */
public interface ServiceListener extends EventListener {
    /**
     * Receives notification that a service has had a lifecycle change.
     *
     * @param event The {@code ServiceEvent} object.
     */
    public void serviceChanged(ServiceEvent event);
}
