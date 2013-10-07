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

import java.util.EventObject;

/**
 * An event from the Runtime describing a module lifecycle change.
 * <p>
 * {@code ModuleEvent} objects are delivered to
 * {@code SynchronousModuleListener}s and {@code ModuleListener}s when a change
 * occurs in a module's lifecycle. A type code is used to identify the event
 * type for future extendability.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @see ModuleListener
 * @see SynchronousModuleListener
 *
 * @Immutable
 */
public class ModuleEvent extends EventObject {

    private static final long serialVersionUID = 1176296577528447208L;

    /**
     * Type of module lifecycle change.
     */
    private final int type;

    /**
     * The module has been installed.
     */
    public final static int INSTALLED = 0x00000001;

    /**
     * The module has been started.
     * <p>
     * The module's {@link ModuleActivator#start(ModuleContext) ModuleActivator
     * start} method has been executed if the module has a module activator
     * class.
     *
     * @see Module#start()
     */
    public final static int STARTED = 0x00000002;

    /**
     * The module has been stopped.
     * <p>
     * The module's {@link ModuleActivator#stop(ModuleContext) ModuleActivator
     * stop} method has been executed if the module has a module activator
     * class.
     *
     * @see Module#stop()
     */
    public final static int STOPPED = 0x00000004;

    /**
     * The module has been uninstalled.
     *
     * @see Module#uninstall()
     */
    public final static int UNINSTALLED = 0x00000010;

    /**
     * The module has been resolved.
     */
    public final static int RESOLVED = 0x00000020;

    /**
     * The module is about to be activated.
     * <p>
     * The module's {@link ModuleActivator#start(ModuleContext) ModuleActivator
     * start} method is about to be called if the module has a module activator
     * class. This event is only delivered to {@link SynchronousModuleListener}
     * s. It is not delivered to {@code ModuleListener}s.
     *
     * @see Module#start()
     */
    public final static int STARTING = 0x00000080;

    /**
     * The module is about to deactivated.
     * <p>
     * The module's {@link ModuleActivator#stop(ModuleContext) ModuleActivator
     * stop} method is about to be called if the module has a module activator
     * class. This event is only delivered to {@link SynchronousModuleListener}
     * s. It is not delivered to {@code ModuleListener}s.
     *
     * @see Module#stop()
     */
    public final static int STOPPING = 0x00000100;

    /**
     * Creates a module event of the specified type.
     *
     * @param type The event type.
     * @param module The module which had a lifecycle change. This module is
     *        used as the origin of the event.
     */
    public ModuleEvent(int type, Module module) {
        super(module);
        this.type = type;
    }

    /**
     * Returns the module which had a lifecycle change. This module is the
     * source of the event.
     *
     * @return The module that had a change occur in its lifecycle.
     */
    public Module getModule() {
        return (Module) getSource();
    }

    /**
     * Returns the type of lifecyle event. The type values are:
     * <ul>
     * <li>{@link #INSTALLED}
     * <li>{@link #RESOLVED}
     * <li>{@link #STARTING}
     * <li>{@link #STARTED}
     * <li>{@link #STOPPING}
     * <li>{@link #STOPPED}
     * <li>{@link #UNINSTALLED}
     * </ul>
     *
     * @return The type of lifecycle event.
     */
    public int getType() {
        return type;
    }
}
