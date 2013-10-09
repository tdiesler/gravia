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
 * A {@code ServiceEvent} listener that does not filter based upon
 * wiring. When a {@code ServiceEvent} is fired, it
 * is synchronously delivered to an {@code AllServiceListener}. The Runtime
 * may deliver {@code ServiceEvent} objects to an {@code AllServiceListener} out
 * of order and may concurrently call and/or reenter an
 * {@code AllServiceListener}.
 * <p>
 * An {@code AllServiceListener} object is registered with the Runtime using
 * the {@code ModuleContext.addServiceListener} method.
 * {@code AllServiceListener} objects are called with a {@code ServiceEvent}
 * object when a service is registered, modified, or is in the process of
 * unregistering.
 *
 * <p>
 * {@code ServiceEvent} object delivery to {@code AllServiceListener} objects is
 * filtered by the filter specified when the listener was registered.
 *
 * <p>
 * Unlike normal {@code ServiceListener} objects, {@code AllServiceListener}
 * objects receive all {@code ServiceEvent} objects regardless of whether the
 * package source of the listening bundle is equal to the package source of the
 * bundle that registered the service. This means that the listener may not be
 * able to cast the service object to any of its corresponding service
 * interfaces if the service object is retrieved.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @ThreadSafe
 */
public interface AllServiceListener extends ServiceListener {
    // This is a marker interface
}
