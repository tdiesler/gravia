/*
 * Copyright (c) OSGi Alliance (2007, 2012). All Rights Reserved.
 *
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
 */

package org.jboss.gravia.runtime;

/**
 * The {@code ModuleTrackerCustomizer} interface allows a {@code ModuleTracker}
 * to customize the {@code Module}s that are tracked. A
 * {@code ModuleTrackerCustomizer} is called when a module is being added to a
 * {@code ModuleTracker}. The {@code ModuleTrackerCustomizer} can then return an
 * object for the tracked module. A {@code ModuleTrackerCustomizer} is also
 * called when a tracked module is modified or has been removed from a
 * {@code ModuleTracker}.
 *
 * <p>
 * The methods in this interface may be called as the result of a
 * {@code ModuleEvent} being received by a {@code ModuleTracker}. Since
 * {@code ModuleEvent}s are received synchronously by the {@code ModuleTracker},
 * it is highly recommended that implementations of these methods do not alter
 * module states while being synchronized on any object.
 *
 * <p>
 * The {@code ModuleTracker} class is thread-safe. It does not call a
 * {@code ModuleTrackerCustomizer} while holding any locks.
 * {@code ModuleTrackerCustomizer} implementations must also be thread-safe.
 *
 * @param <T> The type of the tracked object.
 * @ThreadSafe
 */
public interface ModuleTrackerCustomizer<T> {
    /**
     * A module is being added to the {@code ModuleTracker}.
     *
     * <p>
     * This method is called before a module which matched the search parameters
     * of the {@code ModuleTracker} is added to the {@code ModuleTracker}. This
     * method should return the object to be tracked for the specified
     * {@code Module}. The returned object is stored in the
     * {@code ModuleTracker} and is available from the
     * {@link ModuleTracker#getObject(Module) getObject} method.
     *
     * @param module The {@code Module} being added to the {@code ModuleTracker}
     *        .
     * @param event The module event which caused this customizer method to be
     *        called or {@code null} if there is no module event associated with
     *        the call to this method.
     * @return The object to be tracked for the specified {@code Module} object
     *         or {@code null} if the specified {@code Module} object should not
     *         be tracked.
     */
    public T addingModule(Module module, ModuleEvent event);

    /**
     * A module tracked by the {@code ModuleTracker} has been modified.
     *
     * <p>
     * This method is called when a module being tracked by the
     * {@code ModuleTracker} has had its state modified.
     *
     * @param module The {@code Module} whose state has been modified.
     * @param event The module event which caused this customizer method to be
     *        called or {@code null} if there is no module event associated with
     *        the call to this method.
     * @param object The tracked object for the specified module.
     */
    public void modifiedModule(Module module, ModuleEvent event, T object);

    /**
     * A module tracked by the {@code ModuleTracker} has been removed.
     *
     * <p>
     * This method is called after a module is no longer being tracked by the
     * {@code ModuleTracker}.
     *
     * @param module The {@code Module} that has been removed.
     * @param event The module event which caused this customizer method to be
     *        called or {@code null} if there is no module event associated with
     *        the call to this method.
     * @param object The tracked object for the specified module.
     */
    public void removedModule(Module module, ModuleEvent event, T object);
}
