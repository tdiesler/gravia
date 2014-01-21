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

import java.util.EventListener;

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
