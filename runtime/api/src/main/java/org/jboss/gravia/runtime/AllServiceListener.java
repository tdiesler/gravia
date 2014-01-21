/*
 * #%L
 * Gravia :: Runtime :: API
 * %%
 * Copyright (C) 2013 - 2014 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
