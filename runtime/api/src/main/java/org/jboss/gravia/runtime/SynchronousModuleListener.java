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
/**
 * A synchronous {@code ModuleEvent} listener. {@code SynchronousModuleListener}
 * is a listener interface that may be implemented by a module developer. When a
 * {@code ModuleEvent} is fired, it is synchronously delivered to a
 * {@code SynchronousModuleListener}. The Runtime may deliver
 * {@code ModuleEvent} objects to a {@code SynchronousModuleListener} out of
 * order and may concurrently call and/or reenter a
 * {@code SynchronousModuleListener}.
 *
 * <p>
 * For {@code ModuleEvent} type {@link ModuleEvent#STARTED STARTED}, the Runtime must not
 * hold the referenced module's &quot;state change&quot; lock when the
 * {@code ModuleEvent} is delivered to a {@code SynchronousModuleListener}. For
 * the other {@code ModuleEvent} types, the Runtime must hold the referenced
 * module's &quot;state change&quot; lock when the {@code ModuleEvent} is
 * delivered to a {@code SynchronousModuleListener}. A
 * {@code SynchronousModuleListener} cannot directly call life cycle methods on
 * the referenced module when the Runtime is holding the referenced module's
 * &quot;state change&quot; lock.
 *
 * <p>
 * A {@code SynchronousModuleListener} object is registered with the Runtime
 * using the {@link ModuleContext#addModuleListener(ModuleListener)} method.
 * {@code SynchronousModuleListener} objects are called with a
 * {@code ModuleEvent} object when a module has been installed, resolved,
 * starting, started, stopping, stopped, updated, unresolved, or uninstalled.
 * <p>
 * Unlike normal {@code ModuleListener} objects,
 * {@code SynchronousModuleListener}s are synchronously called during module
 * lifecycle processing. The module lifecycle processing will not proceed until
 * all {@code SynchronousModuleListener}s have completed.
 * {@code SynchronousModuleListener} objects will be called prior to
 * {@code ModuleListener} objects.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @see ModuleEvent
 *
 * @ThreadSafe
 */
public interface SynchronousModuleListener extends ModuleListener {
    // This is a marker interface
}
