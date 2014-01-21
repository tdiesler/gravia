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
 * Allows services to provide customized service objects.
 *
 * <p>
 * When registering a service, a {@code ServiceFactory} object can be used
 * instead of a service object, so that the module developer can gain control of
 * the specific service object granted to a module that is using the service.
 *
 * <p>
 * When this happens, the {@code ModuleContext.getService(ServiceReference)}
 * method calls the {@code ServiceFactory.getService} method to create a service
 * object specifically for the requesting module. The service object returned by
 * the {@code ServiceFactory} is cached by the Runtime until the module
 * releases its use of the service.
 *
 * <p>
 * When the module's use count for the service is decremented to zero (including
 * the module stopping or the service being unregistered), the
 * {@code ServiceFactory.ungetService} method is called.
 *
 * <p>
 * {@code ServiceFactory} objects are only used by the Runtime and are not
 * made available to other modules in the environment. The Runtime may
 * concurrently call a {@code ServiceFactory}.
 *
 * @param <S> Type of Service
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @see ModuleContext#getService(ServiceReference)
 * @ThreadSafe
 */
public interface ServiceFactory<S> {

    /**
     * Creates a new service object.
     *
     * <p>
     * The Runtime invokes this method the first time the specified
     * {@code module} requests a service object using the
     * {@code ModuleContext.getService(ServiceReference)} method. The service
     * factory can then return a specific service object for each module.
     *
     * <p>
     * The Runtime must check that the returned service object is valid. If
     * the returned service object is {@code null} or is not an
     * {@code instanceof} all the classes named when the service was registered,
     * {@code null} is returned to the module.
     *
     * <p>
     * The Runtime caches the valid service object and will return the same
     * service object on any future call to {@code ModuleContext.getService} for
     * the specified module. This means the Runtime must not allow this method
     * to be concurrently called for the specified module.
     *
     * @param module The module requesting the service.
     * @param registration The {@code ServiceRegistration} object for the
     *        requested service.
     * @return A service object that <strong>must</strong> be an instance of all
     *         the classes named when the service was registered.
     * @see ModuleContext#getService(ServiceReference)
     */
    S getService(Module module, ServiceRegistration<S> registration);

    /**
     * Releases a service object.
     *
     * <p>
     * The Runtime invokes this method when a service has been released by a
     * module. The service object may then be destroyed.
     *
     * @param module The module releasing the service.
     * @param registration The {@code ServiceRegistration} object for the
     *        service being released.
     * @param service The service object returned by a previous call to the
     *        {@link #getService(Module, ServiceRegistration) getService}
     *        method.
     * @see ModuleContext#ungetService(ServiceReference)
     */
    void ungetService(Module module, ServiceRegistration<S> registration, S service);
}
