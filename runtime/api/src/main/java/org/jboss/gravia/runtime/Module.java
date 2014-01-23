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

import java.io.File;
import java.net.URL;
import java.util.Dictionary;
import java.util.List;

import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;

/**
 * An installed module in the Runtime.
 *
 * <p>
 * A module must have a unique {@link ResourceIdentity} in the Runtime.
 *
 * <p>
 * Gravia stays out of the business of resolving module's at runtime. Every module
 * has an associated {@link ClassLoader} when it is installed in the runtime already.
 * Modularity must be dealt with in the layer using the Gravia runtime.
 *
 * <p>
 * For example, a servlet container like Tomcat may choose to create one Module per web application.
 * Multiple modules may share the same class loader or may even all have the same class loader association.
 * This allows GRavia to run on a flat class path (e.g. a plain JUnit test)
 *
 * <p>
 * When installed it is also assigned a {@code long} identity, chosen by the Runtime.
 * This identity does not change during the lifecycle of a module.
 * Uninstalling and then reinstalling the module creates a new unique {@code long} identity.
 *
 * <p>
 * A module can be in one of six states:
 * <ul>
 * <li>{@link State#INSTALLED}
 * <li>{@link State#RESOLVED}
 * <li>{@link State#STARTING}
 * <li>{@link State#ACTIVE}
 * <li>{@link State#STOPPING}
 * <li>{@link State#UNINSTALLED}
 * </ul>
 * <p>
 * <p>
 * A module should only have active threads of execution when its state is one
 * of {@code STARTING},{@code ACTIVE}, or {@code STOPPING}.
 * An {@code UNINSTALLED} module can not be set to another state;
 * it can only be reached because references are kept somewhere.
 *
 * <p>
 * The Runtime is the only entity that is allowed to create {@code Module}
 * objects, and these objects are only valid within the Runtime that created
 * them.
 *
 * <p>
 * Modules have a natural ordering such that if two {@code Module}s have the
 * same {@link #getModuleId() module id} they are equal. A {@code Module} is
 * less than another {@code Module} if it has a lower {@link #getModuleId()
 * module id} and is greater if it has a higher module id.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @ThreadSafe
 */
public interface Module {

    /**
     * A module can be in one of six states:
     */
    enum State {
        /**
         * A module is in the {@code INSTALLED} state when it has been installed in the Runtime
         */
        INSTALLED,
        /**
         * The module is in the process of starting.
         * <p>
         * A module is in the {@code STARTING} state when its {@link #start()
         * start} method is active. A module must be in this state when the module's
         * {@link ModuleActivator#start(ModuleContext)} is called. If the
         * {@code ModuleActivator.start(ModuleContext)} method completes without exception, then
         * the module has successfully started and must move to the {@code ACTIVE}
         * state.
         */
        STARTING,
        /**
         * The module is now running.
         * <p>
         * A module is in the {@code ACTIVE} state when it has been successfully
         * started and activated.
         */
        ACTIVE,
        /**
         * The module is in the process of stopping.
         * <p>
         * A module is in the {@code STOPPING} state when its {@link #stop()
         * stop} method is active. A module must be in this state when the module's
         * {@link ModuleActivator#stop(ModuleContext)} method is called. When the
         * {@code ModuleActivator.stop} method completes the module is stopped and
         * must move to the {@code RESOLVED} state.
         */
        STOPPING,
        /**
         * The module is uninstalled and may not be used.
         */
        UNINSTALLED
    }

    /**
     * Addapt this module to another type.
     * <p/>
     * All modules support
     * <ul>
     * <li>{@link Runtime}</li>
     * <li>{@link ClassLoader}</li>
     * <li>{@link Resource}</li>
     * <li>{@link ModuleContext}</li>
     * </ul>
     * @return Null if the module cannot be adapted to the requested type
     */
    <A> A adapt(Class<A> type);

    /**
     * Get the identity of this module.
     */
    ResourceIdentity getIdentity();

    /**
     * Returns this module's unique identifier. Each module is assigned a unique
     * identifier by the Runtime when it was installed.
     *
     * <p>
     * A module's unique identifier has the following attributes:
     * <ul>
     * <li>Is unique and persistent.
     * <li>Is a {@code long}.
     * <li>Its value is not reused for another module, even after a module is uninstalled.
     * <li>Does not change while a module remains installed.
     * </ul>
     *
     * <p>
     * This method must continue to return this module's unique identifier while
     * this module is in the {@code UNINSTALLED} state.
     *
     * @return The unique runtime id of this module.
     */
    long getModuleId();

    /**
     * Returns this module's current state.
     * @see State
     */
    State getState();

    /**
     * Returns this module's {@link ModuleContext}. The returned
     * {@code ModuleContext} can be used by the caller to act on behalf of this
     * module.
     *
     * <p>
     * If this module is not in the {@link State#STARTING}, {@link State#ACTIVE}, or
     * {@link State#STOPPING} states, then this module has no valid {@code ModuleContext}.
     *
     * @return A {@code ModuleContext} for this module or {@code null} if this
     *         module has no valid {@code ModuleContext}.
     */
    ModuleContext getModuleContext();

    /**
     * Returns this module's headers and values that where given on module installation.
     *
     * <p>
     * These values may be mapped to manifest headers, but this is not a requirement.
     * The module's header values do not change durint the lifecycle of the module.
     *
     * <p>
     * Header names are case-insensitive. The methods of the returned
     * {@code Dictionary} object must operate on header names in a
     * case-insensitive manner.
     *
     * <p>
     * This method must continue to return header information while
     * this module is in the {@code UNINSTALLED} state.
     *
     * @return An unmodifiable {@code Dictionary} object containing this
     *         module's Manifest headers and values.
     */
    Dictionary<String, String> getHeaders();

    /**
     * Returns a URL to the entry at the specified path in this module. This
     * modules's class loader is not used to search for the entry. Only the
     * contents of this module are searched for the entry.
     * <p>
     * The specified path is always relative to the root of this module and may
     * begin with &quot;/&quot;. A path value of &quot;/&quot; indicates the
     * root of this module.
     *
     * @param path The path name of the entry.
     * @return A URL to the entry, or {@code null} if no entry could be found.
     */
    URL getEntry(String path);

    /**
     * Returns an Enumeration of all the paths ({@code String} objects) to
     * entries within this module whose longest sub-path matches the specified
     * path. This module's class loader is not used to search for entries. Only
     * the contents of this module are searched.
     * <p>
     * The specified path is always relative to the root of this module and may
     * begin with a &quot;/&quot;. A path value of &quot;/&quot; indicates the
     * root of this module.
     * <p>
     * Returned paths indicating subdirectory paths end with a &quot;/&quot;.
     * The returned paths are all relative to the root of this module and must
     * not begin with &quot;/&quot;.
     *
     * @param path The path name for which to return entry paths.
     * @return A list of the entry paths ({@code String} objects).
     */
    List<String> getEntryPaths(String path);

    /**
     * Returns entries in this module. This module's
     * class loader is not used to search for entries. Only the contents of this
     * module is searched for the specified entries.
     *
     * <p>
     * URLs for directory entries must have their path end with &quot;/&quot;.
     *
     * @param path The path name in which to look. The path is always relative
     *        to the root of this module and may begin with &quot;/&quot;. A
     *        path value of &quot;/&quot; indicates the root of this module.
     * @param filePattern The file name pattern for selecting entries in the
     *        specified path. The pattern is only matched against the last
     *        element of the entry path. If the entry is a directory then the
     *        trailing &quot;/&quot; is not used for pattern matching. Substring
     *        matching is supported, as specified in the Filter specification,
     *        using the wildcard character (&quot;*&quot;). If null is
     *        specified, this is equivalent to &quot;*&quot; and matches all
     *        files.
     * @param recurse If {@code true}, recurse into subdirectories. Otherwise
     *        only return entries from the specified path.
     * @return A list of URL objects for each matching entry.
     */
    List<URL> findEntries(String path, String filePattern, boolean recurse);

    /**
     * Starts this module.
     * <p>
     * If this module's state is {@code UNINSTALLED} then an {@code IllegalStateException} is thrown.
     * <p>
     * The following steps are required to start this module:
     * <ol>
     *
     * <li>If this module is in the process of being activated or deactivated
     * then this method must wait for activation or deactivation to complete
     * before continuing. If this does not occur in a reasonable time, a
     * {@code ModuleException} is thrown to indicate this module was unable to
     * be started.
     *
     * <li>If this module's state is {@code ACTIVE} then this method returns immediately.
     *
     * <li>This module's state is set to {@code STARTING}.
     *
     * <li>A module event of type {@link ModuleEvent#STARTING} is fired.
     *
     * <li>The {@link ModuleActivator#start(ModuleContext)} method if one is specified, is called.
     * If the {@code ModuleActivator} is invalid or throws an exception then:
     * <ul>
     * <li>This module's state is set to {@code STOPPING}.
     * <li>A module event of type {@link ModuleEvent#STOPPING} is fired.
     * <li>Any services registered by this module must be unregistered.
     * <li>Any services used by this module must be released.
     * <li>Any listeners registered by this module must be removed.
     * <li>This module's state is set to {@code RESOLVED}.
     * <li>A module event of type {@link ModuleEvent#STOPPED} is fired.
     * <li>A {@code ModuleException} is then thrown.
     * </ul>
     *
     * <li>This module's state is set to {@code ACTIVE}.
     *
     * <li>A module event of type {@link ModuleEvent#STARTED} is fired.
     * </ol>
     *
     * @throws ModuleException If the module cannot be started
     */
    void start() throws ModuleException;

    /**
     * Stops this module.
     * <p>
     * If this module's state is {@code UNINSTALLED} then an {@code IllegalStateException} is thrown.
     * <p>
     * The following steps are required to stop this module:
     * <ol>
     *
     * <li>If this module is in the process of being activated or deactivated
     * then this method must wait for activation or deactivation to complete
     * before continuing. If this does not occur in a reasonable time, a
     * {@code ModuleException} is thrown to indicate this module was unable to
     * be stopped.
     *
     * <li>If this module's state is not {@code ACTIVE} then this method returns immediately.
     *
     * <li>This module's state is set to {@code STOPPING}.
     *
     * <li>A module event of type {@link ModuleEvent#STOPPING} is fired.
     *
     * <li>The {@link ModuleActivator#stop(ModuleContext)} method of this module's {@code ModuleActivator},
     * if one is specified, is called. If that method throws an exception, this method must continue to
     * stop this module and a {@code ModuleException} must be thrown after
     * completion of the remaining steps.
     *
     * <li>Any services registered by this module must be unregistered.
     * <li>Any services used by this module must be released.
     * <li>Any listeners registered by this module must be removed.
     *
     * <li>This module's state is set to {@code RESOLVED}.
     *
     * <li>A module event of type {@link ModuleEvent#STOPPED} is fired.
     * </ol>
     *
     * @throws ModuleException If the module cannot be started
     */
    void stop() throws ModuleException;

    /**
     * Uninstalls this module.
     *
     * <p>
     * This method causes the Runtime to notify other modules that this module
     * is being uninstalled, and then puts this module into the
     * {@code UNINSTALLED} state. The Runtime must remove any resources
     * related to this module that it is able to remove.
     *
     * <p>
     * If this module's state is {@code UNINSTALLED} then an {@code IllegalStateException} is thrown.
     *
     * <p>
     * The following steps are required to uninstall a module:
     * <ol>
     *
     * <li>This module is stopped as described in the {@code Module.stop} method.
     *
     * <li>This module's state is set to {@code UNINSTALLED}.
     *
     * <li>A module event of type {@link ModuleEvent#UNINSTALLED} is fired.
     * </ol>
     */
    void uninstall();

    /**
     * Loads the specified class using this module's class loader.
     *
     * <p>
     * If this module's state is {@code UNINSTALLED}, then an
     * {@code IllegalStateException} is thrown.
     *
     * @param className The name of the class to load.
     * @return The Class object for the requested class.
     */
    Class<?> loadClass(String className) throws ClassNotFoundException;

    /**
     * Creates a {@code File} object for a file in the persistent storage area
     * provided for this module by the Runtime. This method will return
     * {@code null} if the platform does not have file system support.
     *
     * <p>
     * A {@code File} object for the base directory of the persistent storage
     * area provided for this module by the Runtime can be obtained by calling
     * this method with an empty string as {@code filename}.
     *
     * <p>
     * If the Java Runtime Environment supports permissions, the Runtime will
     * ensure that this module has the {@code java.io.FilePermission} with
     * actions {@code read},{@code write},{@code delete} for all files
     * (recursively) in the persistent storage area provided for this module.
     *
     * @param filename A relative name to the file to be accessed.
     * @return A {@code File} object that represents the requested file or
     *         {@code null} if the platform does not have file system support.
     * @throws IllegalStateException If this module has been uninstalled.
     */
    File getDataFile(String filename);
}
