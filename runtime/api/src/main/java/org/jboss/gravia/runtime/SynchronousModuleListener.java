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
