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
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public class ModuleEvent extends EventObject {

    static final long       serialVersionUID    = 4080640865971756012L;

    /**
     * Module that had a change occur in its lifecycle.
     */
    private final Module    module;

    /**
     * Type of module lifecycle change.
     */
    private final int       type;

    /**
     * The module has been installed.
     * 
     * @see ModuleContext#installModule(String)
     */
    public final static int INSTALLED           = 0x00000001;

    /**
     * The module has been started.
     * <p>
     * The module's {@link ModuleActivator#start(ModuleContext) ModuleActivator
     * start} method has been executed if the module has a module activator
     * class.
     * 
     * @see Module#start()
     */
    public final static int STARTED             = 0x00000002;

    /**
     * The module has been stopped.
     * <p>
     * The module's {@link ModuleActivator#stop(ModuleContext) ModuleActivator
     * stop} method has been executed if the module has a module activator
     * class.
     * 
     * @see Module#stop()
     */
    public final static int STOPPED             = 0x00000004;

    /**
     * The module has been uninstalled.
     * 
     * @see Module#uninstall()
     */
    public final static int UNINSTALLED         = 0x00000010;

    /**
     * The module has been resolved.
     * 
     * @see Module#RESOLVED
     * @since 1.3
     */
    public final static int RESOLVED            = 0x00000020;

    /**
     * The module has been unresolved.
     * 
     * @see Module#INSTALLED
     * @since 1.3
     */
    public final static int UNRESOLVED          = 0x00000040;

    /**
     * The module is about to be activated.
     * <p>
     * The module's {@link ModuleActivator#start(ModuleContext) ModuleActivator
     * start} method is about to be called if the module has a module activator
     * class. This event is only delivered to {@link SynchronousModuleListener}
     * s. It is not delivered to {@code ModuleListener}s.
     * 
     * @see Module#start()
     * @since 1.3
     */
    public final static int STARTING            = 0x00000080;

    /**
     * The module is about to deactivated.
     * <p>
     * The module's {@link ModuleActivator#stop(ModuleContext) ModuleActivator
     * stop} method is about to be called if the module has a module activator
     * class. This event is only delivered to {@link SynchronousModuleListener}
     * s. It is not delivered to {@code ModuleListener}s.
     * 
     * @see Module#stop()
     * @since 1.3
     */
    public final static int STOPPING            = 0x00000100;

    /**
     * Module that was the origin of the event. For install event type, this is
     * the module whose context was used to install the module. Otherwise it is
     * the module itself.
     * 
     * @since 1.6
     */
    private final Module    origin;

    /**
     * Creates a module event of the specified type.
     * 
     * @param type The event type.
     * @param module The module which had a lifecycle change.
     * @param origin The module which is the origin of the event. For the event
     *        type {@link #INSTALLED}, this is the module whose context was used
     *        to install the module. Otherwise it is the module itself.
     * @since 1.6
     */
    public ModuleEvent(int type, Module module, Module origin) {
        super(module);
        if (origin == null) {
            throw new IllegalArgumentException("null origin");
        }
        this.module = module;
        this.type = type;
        this.origin = origin;
    }

    /**
     * Creates a module event of the specified type.
     * 
     * @param type The event type.
     * @param module The module which had a lifecycle change. This module is
     *        used as the origin of the event.
     */
    public ModuleEvent(int type, Module module) {
        super(module);
        this.module = module;
        this.type = type;
        this.origin = module;
    }

    /**
     * Returns the module which had a lifecycle change. This module is the
     * source of the event.
     * 
     * @return The module that had a change occur in its lifecycle.
     */
    public Module getModule() {
        return module;
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
     * <li>{@link #UNRESOLVED}
     * <li>{@link #UNINSTALLED}
     * </ul>
     * 
     * @return The type of lifecycle event.
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the module that was the origin of the event.
     * 
     * <p>
     * For the event type {@link #INSTALLED}, this is the module whose context
     * was used to install the module. Otherwise it is the module itself.
     * 
     * @return The module that was the origin of the event.
     * @since 1.6
     */
    public Module getOrigin() {
        return origin;
    }
}
