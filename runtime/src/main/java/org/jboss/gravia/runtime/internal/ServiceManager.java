/*
 * #%L
 * JBossOSGi Framework
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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
package org.jboss.gravia.runtime.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.gravia.runtime.Module;
import org.jboss.logging.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceException;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;

/**
 * A plugin that manages OSGi services
 *
 * @author thomas.diesler@jboss.com
 * @since 18-Aug-2009
 */
final class ServiceManager {

    private static Logger LOGGER = Logger.getLogger(ServiceManager.class);

    private final RuntimeEvents frameworkEvents;
    private final Map<String, List<ServiceState<?>>> serviceContainer = new HashMap<String, List<ServiceState<?>>>();
    private final ThreadLocal<Module> getServiceRecursion = new ThreadLocal<Module>();
    private final AtomicLong identityGenerator = new AtomicLong();

    ServiceManager(RuntimeEvents frameworkEvents) {
        this.frameworkEvents = frameworkEvents;
    }

    void fireFrameworkEvent(Module bundle, int type, ServiceException ex) {
        frameworkEvents.fireFrameworkEvent(bundle, type, ex);
    }

    void fireServiceEvent(Module bundle, int type, ServiceState<?> serviceState) {
        frameworkEvents.fireServiceEvent(bundle, type, serviceState);
    }

    private long getNextServiceId() {
        return identityGenerator.incrementAndGet();
    }

    /**
     * Registers the specified service object with the specified properties under the specified class names into the Framework.
     * A <code>ServiceRegistration</code> object is returned. The <code>ServiceRegistration</code> object is for the private use
     * of the bundle registering the service and should not be shared with other bundles. The registering bundle is defined to
     * be the context bundle.
     *
     * @param classNames The class names under which the service can be located.
     * @param serviceValue The service object or a <code>ServiceFactory</code> object.
     * @param properties The properties for this service.
     * @return A <code>ServiceRegistration</code> object for use by the bundle registering the service
     */
    @SuppressWarnings({ "rawtypes" })
    ServiceState registerService(Module bundle, String[] classNames, final Object serviceValue, Dictionary properties) {
        assert classNames != null && classNames.length > 0 : "Null service classes";

        ServiceState.ValueProvider<Object> valueProvider = new ServiceState.ValueProvider<Object>() {

            @Override
            public boolean isFactoryValue() {
                return serviceValue instanceof ServiceFactory;
            }

            @Override
            public Object getValue() {
                return serviceValue;
            }
        };

        long serviceId = getNextServiceId();
        ServiceState<?> serviceState = new ServiceState<Object>(this, bundle, serviceId, classNames, valueProvider, properties);
        LOGGER.debugf("Register service: %s", serviceState);

        synchronized (serviceContainer) {
            for (String className : classNames) {
                List<ServiceState<?>> serviceStates = serviceContainer.get(className);
                if (serviceStates != null) {
                    serviceStates.add(serviceState);
                } else {
                    serviceStates = new CopyOnWriteArrayList<ServiceState<?>>();
                    serviceStates.add(serviceState);
                    serviceContainer.put(className, serviceStates);
                }
            }
        }
        //bundle.addRegisteredService(serviceState);

        // This event is synchronously delivered after the service has been registered with the Framework.
        frameworkEvents.fireServiceEvent(bundle, ServiceEvent.REGISTERED, serviceState);

        return serviceState;
    }

    /**
     * Returns a <code>ServiceReference</code> object for a service that implements and was registered under the specified
     * class.
     *
     * @param clazz The class name with which the service was registered.
     * @return A <code>ServiceReference</code> object, or <code>null</code>
     */
    ServiceState<?> getServiceReference(Module bundle, String clazz) {
        assert clazz != null : "Null clazz";

        boolean checkAssignable = (bundle.getModuleId() != 0);
        List<ServiceState<?>> result = getServiceReferencesInternal(bundle, clazz, NoFilter.INSTANCE, checkAssignable);
        if (result.isEmpty())
            return null;

        int lastIndex = result.size() - 1;
        return result.get(lastIndex);
    }

    /**
     * Returns an array of <code>ServiceReference</code> objects. The returned array of <code>ServiceReference</code> objects
     * contains services that were registered under the specified class, match the specified filter expression.
     *
     * If checkAssignable is true, the packages for the class names under which the services were registered match the context
     * bundle's packages as defined in {@link ServiceReference#isAssignableTo(Bundle, String)}.
     *
     *
     * @param clazz The class name with which the service was registered or <code>null</code> for all services.
     * @param filterStr The filter expression or <code>null</code> for all services.
     * @return A potentially empty list of <code>ServiceReference</code> objects.
     */
    List<ServiceState<?>> getServiceReferences(Module bundle, String clazz, String filterStr, boolean checkAssignable) throws InvalidSyntaxException {
        Filter filter = NoFilter.INSTANCE;
        if (filterStr != null)
            filter = FrameworkUtil.createFilter(filterStr);

        List<ServiceState<?>> result = getServiceReferencesInternal(bundle, clazz, filter, checkAssignable);
        return result;
    }

    private List<ServiceState<?>> getServiceReferencesInternal(final Module bundle, String className, Filter filter, boolean checkAssignable) {
        assert bundle != null : "Null bundle";
        assert filter != null : "Null filter";

        Set<ServiceState<?>> initialSet = new HashSet<ServiceState<?>>();
        synchronized (serviceContainer) {
            if (className != null) {
                List<ServiceState<?>> list = serviceContainer.get(className);
                if (list != null) {
                    initialSet.addAll(list);
                }
            } else {
                for (List<ServiceState<?>> list : serviceContainer.values()) {
                    initialSet.addAll(list);
                }
            }
        }

        if (initialSet.isEmpty())
            return Collections.emptyList();

        Set<ServiceState<?>> resultset = new HashSet<ServiceState<?>>();
        for (ServiceState<?> serviceState : initialSet) {
            if (isMatchingService(bundle, serviceState, className, filter, checkAssignable)) {
                resultset.add(serviceState);
            }
        }

        // Sort the result
        List<ServiceState<?>> resultList = new ArrayList<ServiceState<?>>(resultset);
        if (resultList.size() > 1)
            Collections.sort(resultList, ServiceReferenceComparator.getInstance());

        return Collections.unmodifiableList(resultList);
    }

    private boolean isMatchingService(Module bundle, ServiceState<?> serviceState, String clazzName, Filter filter, boolean checkAssignable) {
        if (serviceState.isUnregistered() || filter.match(serviceState) == false)
            return false;
        if (checkAssignable == false || clazzName == null)
            return true;

        return serviceState.isAssignableTo(bundle, clazzName);
    }

    /**
     * Returns the service object referenced by the specified <code>ServiceReference</code> object.
     *
     * @return A service object for the service associated with <code>reference</code> or <code>null</code>
     */
    <S> S getService(Module module, ServiceState<S> serviceState) {
        // If the service has been unregistered, null is returned.
        if (serviceState.isUnregistered())
            return null;

        // If this method is called recursively for the same bundle
        // then it must return null to break the recursion.
        if (getServiceRecursion.get() == module)
            return null;

        S value;
        try {
            getServiceRecursion.set(module);

            // Add the given service ref to the list of used services
            serviceState.addUsingBundle(module);

            value = serviceState.getScopedValue(module);

            // If the factory returned an invalid value
            // restore the service usage counts
            if (value == null) {
                serviceState.removeUsingBundle(module);
            }
        } finally {
            getServiceRecursion.remove();
        }
        return value;
    }

    /**
     * Unregister the given service.
     */

    public void unregisterService(ServiceState<?> serviceState) {
        synchronized (serviceState) {

            if (serviceState.isUnregistered())
                return;

            synchronized (serviceContainer) {
                for (String className : serviceState.getClassNames()) {
                    LOGGER.debugf("Unregister service: %s", className);
                    try {
                        List<ServiceState<?>> serviceStates = serviceContainer.get(className);
                        if (serviceStates != null) {
                            serviceStates.remove(serviceState);
                        }
                    } catch (RuntimeException ex) {
                        LOGGER.errorf(ex, "Cannot unregister service: %s", className);
                    }
                }
            }

            Module serviceOwner = serviceState.getServiceOwner();

            // This event is synchronously delivered before the service has completed unregistering.
            frameworkEvents.fireServiceEvent(serviceOwner, ServiceEvent.UNREGISTERING, serviceState);

            // Remove from using bundles
            for (Module module : serviceState.getUsingBundlesInternal()) {
                while (ungetService(module, serviceState)) {
                }
            }
        }
    }

    /**
     * Releases the service object referenced by the specified <code>ServiceReference</code> object. If the context bundle's use
     * count for the service is zero, this method returns <code>false</code>. Otherwise, the context bundle's use count for the
     * service is decremented by one.
     *
     * @return <code>false</code> if the context bundle's use count for the service is zero or if the service has been
     *         unregistered; <code>true</code> otherwise.
     */

    public boolean ungetService(Module module, ServiceState<?> serviceState) {
        serviceState.ungetScopedValue(module);
        return true;
    }
}