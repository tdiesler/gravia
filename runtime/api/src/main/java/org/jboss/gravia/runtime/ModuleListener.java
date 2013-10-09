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
 * A {@code ModuleEvent} listener. {@code ModuleListener} is a listener
 * interface that may be implemented by a module developer. When a
 * {@code ModuleEvent} is fired, it is asynchronously delivered to a
 * {@code ModuleListener}. The Runtime delivers {@code ModuleEvent} objects to
 * a {@code ModuleListener} in order and must not concurrently call a
 * {@code ModuleListener}.
 * <p>
 * A {@code ModuleListener} object is registered with the Runtime using the
 * {@link ModuleContext#addModuleListener(ModuleListener)} method.
 * {@code ModuleListener}s are called with a {@code ModuleEvent} object when a
 * module has been installed, resolved, started, stopped or uninstalled.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 * @see ModuleEvent
 * @NotThreadSafe
 */
public interface ModuleListener extends EventListener {

    /**
     * Receives notification that a module has had a lifecycle change.
     *
     * @param event The {@code ModuleEvent}.
     */
    public void moduleChanged(ModuleEvent event);
}
