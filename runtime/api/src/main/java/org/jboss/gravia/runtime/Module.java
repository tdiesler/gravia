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

import java.io.File;
import java.util.Dictionary;

import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.AttachmentKey;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.spi.ModuleEntriesProvider;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public interface Module extends Attachable {

    AttachmentKey<ModuleEntriesProvider> ENTRIES_PROVIDER_KEY = AttachmentKey.create(ModuleEntriesProvider.class);

    enum State {
        INSTALLED,
        RESOLVED,
        STARTING,
        ACTIVE,
        STOPPING,
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
     * </ul>
     * @return Null if the module cannot be adapted to the requested type
     */
    <A> A adapt(Class<A> type);

    ResourceIdentity getIdentity();

    long getModuleId();

    State getState();

    ModuleContext getModuleContext();

    Dictionary<String, String> getHeaders();

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
     * <b>Preconditions </b>
     * <ul>
     * <li>{@code getState()} in &#x007B; {@code ACTIVE} &#x007D;.
     * </ul>
     * <b>Postconditions, no exceptions thrown </b>
     * <ul>
     * <li>Module autostart setting is modified unless the
     * {@link #STOP_TRANSIENT} option was set.
     * <li>{@code getState()} not in &#x007B; {@code ACTIVE}, {@code STOPPING}
     * &#x007D;.
     * <li>{@code ModuleActivator.stop} has been called and did not throw an
     * exception.
     * </ul>
     * <b>Postconditions, when an exception is thrown </b>
     * <ul>
     * <li>Module autostart setting is modified unless the
     * {@link #STOP_TRANSIENT} option was set.
     * </ul>
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

    Class<?> loadClass(String className) throws ClassNotFoundException;

    File getDataFile(String filename);
}
