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

import java.util.Collection;
import java.util.Dictionary;


/**
 * A module's execution context within the Runtime. The context is used to
 * grant access to other methods so that this module can interact with the
 * Runtime.
 *
 * <p>
 * {@code ModuleContext} methods allow a module to:
 * <ul>
 * <li>Subscribe to events published by the Runtime.
 * <li>Register service objects with the Runtime service registry.
 * <li>Retrieve {@code ServiceReferences} from the Runtime service registry.
 * <li>Get and release service objects for a referenced service.
 * <li>Create {@code File} objects for files in a persistent storage area provided for the module by the Runtime.
 * </ul>
 *
 * <p>
 * A {@code ModuleContext} object will be created for a module when the module
 * is started. The {@code Module} object associated with a {@code ModuleContext}
 * object is called the <em>context module</em>.
 *
 * <p>
 * The {@code ModuleContext} object will be passed to the
 * {@link ModuleActivator#start(ModuleContext)} method during activation of the
 * context module. The same {@code ModuleContext} object will be passed to the
 * {@link ModuleActivator#stop(ModuleContext)} method when the context module is
 * stopped. A {@code ModuleContext} object is generally for the private use of
 * its associated module and is not meant to be shared with other modules in the
 * environment.
 *
 * <p>
 * The {@code ModuleContext} object is only valid during the execution of its
 * context module; that is, during the period from when the context module is in
 * the {@code STARTING}, {@code STOPPING}, and {@code ACTIVE} module states. If
 * the {@code ModuleContext} object is used subsequently, an
 * {@code IllegalStateException} must be thrown. The {@code ModuleContext}
 * object must never be reused after its context module is stopped.
 *
 * <p>
 * Two {@code ModuleContext} objects are equal if they both refer to the same
 * execution context of a module. The Runtime is the only entity that can
 * create {@code ModuleContext} objects and they are only valid within the
 * Runtime that created them.
 *
 * <p>
 * A {@link Module} can be {@link Module#adapt(Class) adapted} to its
 * {@code ModuleContext}.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @ThreadSafe
 */
public interface ModuleContext {

    /**
     * Get the module associated with this context
     */
    Module getModule();

    /**
     * Creates a {@code Filter} object. This {@code Filter} object may be used
     * to match a {@code ServiceReference} object or a {@code Dictionary}
     * object.
     *
     * <p>
     * If the filter cannot be parsed, an {@link IllegalArgumentException} will be
     * thrown with a human readable message where the filter became unparsable.
     *
     * @param filter The filter string.
     * @return A {@code Filter} object encapsulating the filter string.
     */
    Filter createFilter(String filter);

    /**
     * Adds the specified {@code ModuleListener} object to the context module's
     * list of listeners if not already present. ModuleListener objects are
     * notified when a module has a lifecycle state change.
     *
     * <p>
     * If the context module's list of listeners already contains a listener
     * {@code l} such that {@code (l==listener)}, this method does nothing.
     *
     * @param listener The {@code ModuleListener} to be added.
     * @throws IllegalStateException If this ModuleContext is no longer valid.
     * @see ModuleEvent
     * @see ModuleListener
     */
    void addModuleListener(ModuleListener listener);

    /**
     * Removes the specified {@code ModuleListener} object from the context
     * module's list of listeners.
     *
     * <p>
     * If {@code listener} is not contained in the context module's list of
     * listeners, this method does nothing.
     *
     * @param listener The {@code ModuleListener} object to be removed.
     * @throws IllegalStateException If this ModuleContext is no longer valid.
     */
    void removeModuleListener(ModuleListener listener);

    /**
     * Adds the specified {@code ServiceListener} object with the specified
     * {@code filter} to the context module's list of listeners. See
     * {@link Filter} for a description of the filter syntax.
     * {@code ServiceListener} objects are notified when a service has a
     * lifecycle state change.
     *
     * <p>
     * If the context module's list of listeners already contains a listener
     * {@code l} such that {@code (l==listener)}, then this method replaces that
     * listener's filter (which may be {@code null}) with the specified one
     * (which may be {@code null}).
     *
     * <p>
     * The listener is called if the filter criteria is met. To filter based
     * upon the class of the service, the filter should reference the
     * {@link Constants#OBJECTCLASS} property. If {@code filter} is {@code null}
     * , all services are considered to match the filter.
     *
     * <p>
     * When using a {@code filter}, it is possible that the {@code ServiceEvent}
     * s for the complete lifecycle of a service will not be delivered to the
     * listener. For example, if the {@code filter} only matches when the
     * property {@code x} has the value {@code 1}, the listener will not be
     * called if the service is registered with the property {@code x} not set
     * to the value {@code 1}. Subsequently, when the service is modified
     * setting property {@code x} to the value {@code 1}, the filter will match
     * and the listener will be called with a {@code ServiceEvent} of type
     * {@code MODIFIED}. Thus, the listener will not be called with a
     * {@code ServiceEvent} of type {@code REGISTERED}.
     *
     * @param listener The {@code ServiceListener} object to be added.
     * @param filter The filter criteria.
     * @throws IllegalArgumentException If {@code filter} contains an invalid
     *         filter string that cannot be parsed.
     * @throws IllegalStateException If this ModuleContext is no longer valid.
     * @see ServiceEvent
     * @see ServiceListener
     */
    void addServiceListener(ServiceListener listener, String filter);

    /**
     * Adds the specified {@code ServiceListener} object to the context module's
     * list of listeners.
     *
     * <p>
     * This method is the same as calling
     * {@code ModuleContext.addServiceListener(ServiceListener listener,
     * String filter)} with {@code filter} set to {@code null}.
     *
     * @param listener The {@code ServiceListener} object to be added.
     * @throws IllegalStateException If this ModuleContext is no longer valid.
     * @see #addServiceListener(ServiceListener, String)
     */
    void addServiceListener(ServiceListener listener);

    /**
     * Removes the specified {@code ServiceListener} object from the context
     * module's list of listeners.
     *
     * <p>
     * If {@code listener} is not contained in this context module's list of
     * listeners, this method does nothing.
     *
     * @param listener The {@code ServiceListener} to be removed.
     * @throws IllegalStateException If this ModuleContext is no longer valid.
     */
    void removeServiceListener(ServiceListener listener);

    /**
     * Registers the specified service object with the specified properties
     * under the name of the specified class with the Runtime.
     *
     * <p>
     * This method is otherwise identical to
     * {@link #registerService(String, Object, Dictionary)} and is provided to
     * return a type safe {@code ServiceRegistration}.
     *
     * @param <S> Type of Service.
     * @param clazz The class under whose name the service can be located.
     * @param service The service object or a {@code ServiceFactory} object.
     * @param properties The properties for this service.
     * @return A {@code ServiceRegistration} object for use by the module
     *         registering the service to update the service's properties or to
     *         unregister the service.
     * @throws IllegalStateException If this ModuleContext is no longer valid.
     * @see #registerService(String, Object, Dictionary)
     */
    <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties);

    /**
     * Registers the specified service object with the specified properties
     * under the specified class name with the Runtime.
     *
     * <p>
     * This method is otherwise identical to
     * {@link #registerService(String[], Object, Dictionary)} and is provided as
     * a convenience when {@code service} will only be registered under a single
     * class name. Note that even in this case the value of the service's
     * {@link Constants#OBJECTCLASS} property will be an array of string, rather
     * than just a single string.
     *
     * @param className The class name under which the service can be located.
     * @param service The service object or a {@code ServiceFactory} object.
     * @param properties The properties for this service.
     * @return A {@code ServiceRegistration} object for use by the module
     *         registering the service to update the service's properties or to
     *         unregister the service.
     * @throws IllegalStateException If this ModuleContext is no longer valid.
     * @see #registerService(String[], Object, Dictionary)
     */
    ServiceRegistration<?> registerService(String className, Object service, Dictionary<String, ?> properties);

    /**
     * Registers the specified service object with the specified properties
     * under the specified class names into the Runtime. A
     * {@code ServiceRegistration} object is returned. The
     * {@code ServiceRegistration} object is for the private use of the module
     * registering the service and should not be shared with other modules. The
     * registering module is defined to be the context module.
     *
     * <p>
     * A module can register a service object that implements the
     * {@link ServiceFactory} interface to have more flexibility in providing
     * service objects to other modules.
     *
     * <p>
     * The following steps are required to register a service:
     * <ol>
     * <li>If {@code service} is not a {@code ServiceFactory}, an
     * {@code IllegalArgumentException} is thrown if {@code service} is not an
     * {@code instanceof} all the specified class names.
     * <li>The Runtime adds the following service properties to the service
     * properties from the specified {@code Dictionary} (which may be
     * {@code null}): <br/>
     * A property named {@link Constants#SERVICE_ID} identifying the
     * registration number of the service <br/>
     * A property named {@link Constants#OBJECTCLASS} containing all the
     * specified classes. <br/>
     * Properties with these names in the specified {@code Dictionary} will be
     * ignored.
     * <li>The service is added to the Runtime service registry and may now be
     * used by other modules.
     * <li>A service event of type {@link ServiceEvent#REGISTERED} is fired.
     * <li>A {@code ServiceRegistration} object for this registration is
     * returned.
     * </ol>
     *
     * @param classNames The class names under which the service can be located.
     *        The class names in this array will be stored in the service's
     *        properties under the key {@link Constants#OBJECTCLASS}.
     * @param service The service object or a {@code ServiceFactory} object.
     * @param properties The properties for this service. The keys in the
     *        properties object must all be {@code String} objects. See
     *        {@link Constants} for a list of standard service property keys.
     *        Changes should not be made to this object after calling this
     *        method. To update the service's properties the
     *        {@link ServiceRegistration#setProperties(Dictionary)} method must
     *        be called. The set of properties may be {@code null} if the
     *        service has no properties.
     * @return A {@code ServiceRegistration} object for use by the module
     *         registering the service to update the service's properties or to
     *         unregister the service.
     * @throws IllegalArgumentException If one of the following is true:
     *         <ul>
     *         <li>{@code service} is {@code null}. <li>{@code service} is not a
     *         {@code ServiceFactory} object and is not an instance of all the
     *         named classes in {@code clazzes}. <li> {@code properties}
     *         contains case variants of the same key name.
     *         </ul>
     * @throws IllegalStateException If this ModuleContext is no longer valid.
     * @see ServiceRegistration
     * @see ServiceFactory
     */
    ServiceRegistration<?> registerService(String[] classNames, Object service, Dictionary<String, ?> properties);

    /**
     * Returns a {@code ServiceReference} object for a service that implements
     * and was registered under the name of the specified class.
     *
     * <p>
     * The returned {@code ServiceReference} object is valid at the time of the
     * call to this method. However as the Runtime is a very dynamic
     * environment, services can be modified or unregistered at any time.
     *
     * <p>
     * This method is the same as calling
     * {@link #getServiceReferences(Class, String)} with a {@code null} filter
     * expression. It is provided as a convenience for when the caller is
     * interested in any service that implements the specified class.
     * <p>
     * If multiple such services exist, the service with the highest ranking (as
     * specified in its {@link Constants#SERVICE_RANKING} property) is returned.
     * <p>
     * If there is a tie in ranking, the service with the lowest service ID (as
     * specified in its {@link Constants#SERVICE_ID} property); that is, the
     * service that was registered first is returned.
     *
     * @param <S> Type of Service.
     * @param clazz The class under whose name the service was registered. Must
     *        not be {@code null}.
     * @return A {@code ServiceReference} object, or {@code null} if no services
     *         are registered which implement the specified class.
     * @throws IllegalStateException If this ModuleContext is no longer valid.
     * @see #getServiceReferences(Class, String)
     */
    <S> ServiceReference<S> getServiceReference(Class<S> clazz);

    /**
     * Returns a {@code ServiceReference} object for a service that implements
     * and was registered under the specified class.
     *
     * <p>
     * The returned {@code ServiceReference} object is valid at the time of the
     * call to this method. However as the Runtime is a very dynamic
     * environment, services can be modified or unregistered at any time.
     *
     * <p>
     * This method is the same as calling
     * {@link #getServiceReferences(String, String)} with a {@code null} filter
     * expression and then finding the reference with the highest priority. It
     * is provided as a convenience for when the caller is interested in any
     * service that implements the specified class.
     * <p>
     * If multiple such services exist, the service with the highest priority is
     * selected. This priority is defined as the service reference with the
     * highest ranking (as specified in its {@link Constants#SERVICE_RANKING}
     * property) is returned.
     * <p>
     * If there is a tie in ranking, the service with the lowest service ID (as
     * specified in its {@link Constants#SERVICE_ID} property); that is, the
     * service that was registered first is returned.
     *
     * @param className The class name with which the service was registered.
     * @return A {@code ServiceReference} object, or {@code null} if no services
     *         are registered which implement the named class.
     * @throws IllegalStateException If this ModuleContext is no longer valid.
     * @see #getServiceReferences(String, String)
     */
    ServiceReference<?> getServiceReference(String className);

    /**
     * Returns a collection of {@code ServiceReference} objects. The returned
     * collection of {@code ServiceReference} objects contains services that
     * were registered under the name of the specified class, match the
     * specified filter expression, and the packages for the class names under
     * which the services were registered match the context module's packages as
     * defined in {@link ServiceReference#isAssignableTo(Module, String)}.
     *
     * <p>
     * The collection is valid at the time of the call to this method. However
     * since the Runtime is a very dynamic environment, services can be
     * modified or unregistered at any time.
     *
     * <p>
     * The specified {@code filter} expression is used to select the registered
     * services whose service properties contain keys and values which satisfy
     * the filter expression. See {@link Filter} for a description of the filter
     * syntax. If the specified {@code filter} is {@code null}, all registered
     * services are considered to match the filter. If the specified
     * {@code filter} expression cannot be parsed, an
     * {@link IllegalArgumentException} will be thrown with a human readable
     * message where the filter became unparsable.
     *
     * <p>
     * The result is a collection of {@code ServiceReference} objects for all
     * services that meet all of the following conditions:
     * <ul>
     * <li>The service must have been registered with the name of the specified
     * class. The complete list of class names with which a service was
     * registered is available from the service's {@link Constants#OBJECTCLASS
     * objectClass} property.
     * <li>If the specified {@code filter} is not {@code null}, the filter
     * expression must match the service.
     * <li>For each class name with which the service was registered, calling
     * {@link ServiceReference#isAssignableTo(Module, String)} with the context
     * module and the class name on the service's {@code ServiceReference}
     * object must return {@code true}
     * </ul>
     *
     * @param <S> Type of Service
     * @param clazz The class under whose name the service was registered. Must
     *        not be {@code null}.
     * @param filter The filter expression or {@code null} for all services.
     * @return A collection of {@code ServiceReference} objects. May be empty if
     *         no services are registered which satisfy the search.
     * @throws IllegalArgumentException If the specified {@code filter} contains
     *         an invalid filter expression that cannot be parsed.
     * @throws IllegalStateException If this ModuleContext is no longer valid.
     */
    <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter);

    /**
     * Returns an array of {@code ServiceReference} objects. The returned array
     * of {@code ServiceReference} objects contains services that were
     * registered under the specified class, match the specified filter
     * expression, and the packages for the class names under which the services
     * were registered match the context module's packages as defined in
     * {@link ServiceReference#isAssignableTo(Module, String)}.
     *
     * <p>
     * The list is valid at the time of the call to this method. However since
     * the Runtime is a very dynamic environment, services can be modified or
     * unregistered at any time.
     *
     * <p>
     * The specified {@code filter} expression is used to select the registered
     * services whose service properties contain keys and values which satisfy
     * the filter expression. See {@link Filter} for a description of the filter
     * syntax. If the specified {@code filter} is {@code null}, all registered
     * services are considered to match the filter. If the specified
     * {@code filter} expression cannot be parsed, an
     * {@link IllegalArgumentException} will be thrown with a human readable
     * message where the filter became unparsable.
     *
     * <p>
     * The result is an array of {@code ServiceReference} objects for all
     * services that meet all of the following conditions:
     * <ul>
     * <li>If the specified class name, {@code clazz}, is not {@code null}, the
     * service must have been registered with the specified class name. The
     * complete list of class names with which a service was registered is
     * available from the service's {@link Constants#OBJECTCLASS objectClass}
     * property.
     * <li>If the specified {@code filter} is not {@code null}, the filter
     * expression must match the service.
     * <li>For each class name with which the service was registered, calling
     * {@link ServiceReference#isAssignableTo(Module, String)} with the context
     * module and the class name on the service's {@code ServiceReference}
     * object must return {@code true}
     * </ul>
     *
     * @param className The class name with which the service was registered or
     *        {@code null} for all services.
     * @param filter The filter expression or {@code null} for all services.
     * @return An array of {@code ServiceReference} objects or {@code null} if
     *         no services are registered which satisfy the search.
     * @throws IllegalArgumentException If the specified {@code filter} contains
     *         an invalid filter expression that cannot be parsed.
     * @throws IllegalStateException If this ModuleContext is no longer valid.
     */
    ServiceReference<?>[] getServiceReferences(String className, String filter);

    /**
     * Returns an array of {@code ServiceReference} objects. The returned array
     * of {@code ServiceReference} objects contains services that were
     * registered under the specified class and match the specified filter
     * expression.
     *
     * <p>
     * The list is valid at the time of the call to this method. However since
     * the Runtime is a very dynamic environment, services can be modified or
     * unregistered at any time.
     *
     * <p>
     * The specified {@code filter} expression is used to select the registered
     * services whose service properties contain keys and values which satisfy
     * the filter expression. See {@link Filter} for a description of the filter
     * syntax. If the specified {@code filter} is {@code null}, all registered
     * services are considered to match the filter. If the specified
     * {@code filter} expression cannot be parsed, an
     * {@link IllegalArgumentException} will be thrown with a human readable
     * message where the filter became unparsable.
     *
     * <p>
     * The result is an array of {@code ServiceReference} objects for all
     * services that meet all of the following conditions:
     * <ul>
     * <li>If the specified class name, {@code clazz}, is not {@code null}, the
     * service must have been registered with the specified class name. The
     * complete list of class names with which a service was registered is
     * available from the service's {@link Constants#OBJECTCLASS objectClass}
     * property.
     * <li>If the specified {@code filter} is not {@code null}, the filter
     * expression must match the service.
     * </ul>
     *
     * @param className The class name with which the service was registered or
     *        {@code null} for all services.
     * @param filter The filter expression or {@code null} for all services.
     * @return An array of {@code ServiceReference} objects or {@code null} if
     *         no services are registered which satisfy the search.
     * @throws IllegalArgumentException If the specified {@code filter} contains
     *         an invalid filter expression that cannot be parsed.
     * @throws IllegalStateException If this ModuleContext is no longer valid.
     */
    ServiceReference<?>[] getAllServiceReferences(String className, String filter);

    /**
     * Returns the service object referenced by the specified
     * {@code ServiceReference} object.
     * <p>
     * A module's use of a service is tracked by the module's use count of that
     * service. Each time a service's service object is returned by
     * {@link #getService(ServiceReference)} the context module's use count for
     * that service is incremented by one. Each time the service is released by
     * {@link #ungetService(ServiceReference)} the context module's use count
     * for that service is decremented by one.
     * <p>
     * When a module's use count for a service drops to zero, the module should
     * no longer use that service.
     *
     * <p>
     * This method will always return {@code null} when the service associated
     * with this {@code reference} has been unregistered.
     *
     * <p>
     * The following steps are required to get the service object:
     * <ol>
     * <li>If the service has been unregistered, {@code null} is returned.
     * <li>If the context module's use count for the service is currently zero
     * and the service was registered with an object implementing the
     * {@code ServiceFactory} interface, the
     * {@link ServiceFactory#getService(Module, ServiceRegistration)} method is
     * called to create a service object for the context module. If the service
     * object returned by the {@code ServiceFactory} object is {@code null}, not
     * an {@code instanceof} all the classes named when the service was
     * registered or the {@code ServiceFactory} object throws an exception or
     * will be recursively called for the context module, {@code null} is
     * returned.
     * <br>
     * This service object is cached by the Runtime. While the context
     * module's use count for the service is greater than zero, subsequent calls
     * to get the services's service object for the context module will return
     * the cached service object.
     * <li>The context module's use count for this service is incremented by
     * one.
     * <li>The service object for the service is returned.
     * </ol>
     *
     * @param <S> Type of Service.
     * @param reference A reference to the service.
     * @return A service object for the service associated with
     *         {@code reference} or {@code null} if the service is not
     *         registered, the service object returned by a
     *         {@code ServiceFactory} does not implement the classes under which
     *         it was registered or the {@code ServiceFactory} threw an
     *         exception.
     * @throws IllegalStateException If this ModuleContext is no longer valid.
     * @throws IllegalArgumentException If the specified
     *         {@code ServiceReference} was not created by the same framework
     *         instance as this {@code ModuleContext}.
     * @see #ungetService(ServiceReference)
     * @see ServiceFactory
     */
    <S> S getService(ServiceReference<S> reference);

    /**
     * Releases the service object referenced by the specified
     * {@code ServiceReference} object. If the context module's use count for
     * the service is zero, this method returns {@code false}. Otherwise, the
     * context module's use count for the service is decremented by one.
     *
     * <p>
     * The service's service object should no longer be used and all references
     * to it should be destroyed when a module's use count for the service drops
     * to zero.
     *
     * <p>
     * The following steps are required to unget the service object:
     * <ol>
     * <li>If the context module's use count for the service is zero or the
     * service has been unregistered, {@code false} is returned.
     * <li>The context module's use count for this service is decremented by
     * one.
     * <li>If the context module's use count for the service is currently zero
     * and the service was registered with a {@code ServiceFactory} object, the
     * {@link ServiceFactory#ungetService(Module, ServiceRegistration, Object)}
     * method is called to release the service object for the context module.
     * <li>{@code true} is returned.
     * </ol>
     *
     * @param reference A reference to the service to be released.
     * @return {@code false} if the context module's use count for the service
     *         is zero or if the service has been unregistered; {@code true}
     *         otherwise.
     * @throws IllegalStateException If this ModuleContext is no longer valid.
     * @throws IllegalArgumentException If the specified
     *         {@code ServiceReference} was not created by the same framework
     *         instance as this {@code ModuleContext}.
     * @see #getService(ServiceReference)
     * @see ServiceFactory
     */
    boolean ungetService(ServiceReference<?> reference);
}
