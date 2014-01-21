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
