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
 * Customizes the starting and stopping of a module.
 * <p>
 * {@code ModuleActivator} is an interface that may be implemented when a module
 * is started or stopped. The Runtime can create instances of a module's
 * {@code ModuleActivator} as required. If an instance's
 * {@code ModuleActivator.start} method executes successfully, it is guaranteed
 * that the same instance's {@code ModuleActivator.stop} method will be called
 * when the module is to be stopped. The Runtime must not concurrently call a
 * {@code ModuleActivator} object.
 *
 * <p>
 * {@code ModuleActivator} is specified through the {@code Module-Activator}
 * module header.
 *
 * <p>
 * The specified {@code ModuleActivator} class must have a public constructor
 * that takes no parameters so that a {@code ModuleActivator} object can be
 * created by {@code Class.newInstance()}.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @NotThreadSafe
 */
public interface ModuleActivator {

    /**
     * Called when this module is started so the Runtime can perform the
     * module-specific activities necessary to start this module. This method
     * can be used to register services or to allocate any resources that this
     * module needs.
     *
     * <p>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context The execution context of the module being started.
     * @throws Exception If this method throws an exception, this module is
     *         marked as stopped and the Runtime will remove this module's
     *         listeners, unregister all services registered by this module, and
     *         release all services used by this module.
     */
    void start(ModuleContext context) throws Exception;

    /**
     * Called when this module is stopped so the Runtime can perform the
     * module-specific activities necessary to stop the module. In general, this
     * method should undo the work that the {@code ModuleActivator.start} method
     * started. There should be no active threads that were started by this
     * module when this module returns. A stopped module must not call any
     * Runtime objects.
     *
     * <p>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context The execution context of the module being stopped.
     * @throws Exception If this method throws an exception, the module is still
     *         marked as stopped, and the Runtime will remove the module's
     *         listeners, unregister all services registered by the module, and
     *         release all services used by the module.
     */
    void stop(ModuleContext context) throws Exception;
}
