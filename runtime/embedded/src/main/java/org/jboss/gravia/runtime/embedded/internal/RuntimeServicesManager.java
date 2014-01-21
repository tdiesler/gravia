/*
 * #%L
 * Gravia :: Runtime :: Embedded
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
package org.jboss.gravia.runtime.embedded.internal;

import static org.jboss.gravia.runtime.spi.RuntimeLogger.LOGGER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.gravia.runtime.Filter;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceEvent;
import org.jboss.gravia.runtime.ServiceFactory;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.spi.AbstractModule;
import org.jboss.gravia.runtime.spi.RuntimeEventsManager;
import org.jboss.gravia.runtime.util.NoFilter;
import org.osgi.framework.Bundle;

/**
 * Manages the services in hte runtime
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @ThreadSafe
 */
final class RuntimeServicesManager {

    private final RuntimeEventsManager frameworkEvents;
    private final Map<String, List<ServiceState<?>>> serviceMap = new ConcurrentHashMap<String, List<ServiceState<?>>>();
    private final AtomicLong identityGenerator = new AtomicLong();

    RuntimeServicesManager(RuntimeEventsManager frameworkEvents) {
        this.frameworkEvents = frameworkEvents;
    }

    void fireServiceEvent(Module module, int type, ServiceState<?> serviceState) {
        frameworkEvents.fireServiceEvent(module, type, serviceState);
    }

    private long getNextServiceId() {
        return identityGenerator.incrementAndGet();
    }

    /**
     * Registers the specified service object with the specified properties under the specified class names into the Framework.
     * A <code>ServiceRegistration</code> object is returned. The <code>ServiceRegistration</code> object is for the private use
     * of the module registering the service and should not be shared with other modules. The registering module is defined to
     * be the context module.
     *
     * @param classNames The class names under which the service can be located.
     * @param serviceValue The service object or a <code>ServiceFactory</code> object.
     * @param properties The properties for this service.
     * @return A <code>ServiceRegistration</code> object for use by the module registering the service
     */
    @SuppressWarnings({ "rawtypes" })
    ServiceState registerService(ModuleContext context, String[] classNames, final Object serviceValue, Dictionary properties) {
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
        ServiceState<?> serviceState = new ServiceState<Object>(this, context.getModule(), serviceId, classNames, valueProvider, properties);
        LOGGER.debug("Register service: {}", serviceState);

        for (String className : classNames) {
            List<ServiceState<?>> serviceStates;
            synchronized (serviceMap) {
                serviceStates = serviceMap.get(className);
                if (serviceStates == null) {
                    serviceStates = new CopyOnWriteArrayList<ServiceState<?>>();
                    serviceMap.put(className, serviceStates);
                }
            }
            serviceStates.add(serviceState);
        }
        //module.addRegisteredService(serviceState);

        // This event is synchronously delivered after the service has been registered with the Framework.
        frameworkEvents.fireServiceEvent(context.getModule(), ServiceEvent.REGISTERED, serviceState);

        return serviceState;
    }

    /**
     * Returns a <code>ServiceReference</code> object for a service that implements and was registered under the specified
     * class.
     *
     * @param clazz The class name with which the service was registered.
     * @return A <code>ServiceReference</code> object, or <code>null</code>
     */
    ServiceState<?> getServiceReference(ModuleContext context, String clazz) {
        assert clazz != null : "Null clazz";

        List<ServiceState<?>> result = getServiceReferencesInternal(context, clazz, NoFilter.INSTANCE, true);
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
     * module's packages as defined in {@link ServiceReference#isAssignableTo(Bundle, String)}.
     *
     *
     * @param clazz The class name with which the service was registered or <code>null</code> for all services.
     * @param filterStr The filter expression or <code>null</code> for all services.
     * @return A potentially empty list of <code>ServiceReference</code> objects.
     */
    List<ServiceState<?>> getServiceReferences(ModuleContext context, String clazz, String filterStr, boolean checkAssignable) {
        Filter filter = NoFilter.INSTANCE;
        if (filterStr != null)
            filter = context.createFilter(filterStr);

        List<ServiceState<?>> result = getServiceReferencesInternal(context, clazz, filter, checkAssignable);
        return result;
    }

    private List<ServiceState<?>> getServiceReferencesInternal(final ModuleContext module, String className, Filter filter, boolean checkAssignable) {
        assert module != null : "Null module";
        assert filter != null : "Null filter";

        Set<ServiceState<?>> initialSet = new HashSet<ServiceState<?>>();
        if (className != null) {
            List<ServiceState<?>> list = serviceMap.get(className);
            if (list != null) {
                initialSet.addAll(list);
            }
        } else {
            for (List<ServiceState<?>> list : serviceMap.values()) {
                initialSet.addAll(list);
            }
        }

        if (initialSet.isEmpty())
            return Collections.emptyList();

        Set<ServiceState<?>> resultset = new HashSet<ServiceState<?>>();
        for (ServiceState<?> serviceState : initialSet) {
            if (isMatchingService(module, serviceState, className, filter, checkAssignable)) {
                resultset.add(serviceState);
            }
        }

        // Sort the result
        List<ServiceState<?>> resultList = new ArrayList<ServiceState<?>>(resultset);
        if (resultList.size() > 1)
            Collections.sort(resultList, ServiceReferenceComparator.getInstance());

        return Collections.unmodifiableList(resultList);
    }

    private boolean isMatchingService(ModuleContext context, ServiceState<?> serviceState, String clazzName, Filter filter, boolean checkAssignable) {
        if (serviceState.isUnregistered() || filter.match(serviceState) == false)
            return false;
        if (checkAssignable == false || clazzName == null)
            return true;

        return serviceState.isAssignableTo(context.getModule(), clazzName);
    }

    /**
     * Returns the service object referenced by the specified <code>ServiceReference</code> object.
     *
     * @return A service object for the service associated with <code>reference</code> or <code>null</code>
     */
    <S> S getService(ModuleContext context, ServiceState<S> serviceState) {
        // If the service has been unregistered, null is returned.
        if (serviceState.isUnregistered())
            return null;

        // Add the given service ref to the list of used services
        serviceState.addUsingModule((AbstractModule) context.getModule());

        S value = serviceState.getScopedValue(context.getModule());

        // If the factory returned an invalid value
        // restore the service usage counts
        if (value == null) {
            serviceState.removeUsingModule((AbstractModule) context.getModule());
        }

        return value;
    }

    /**
     * Unregister the given service.
     */
    void unregisterService(ServiceState<?> serviceState) {
        if (serviceState.isUnregistered())
            return;

        LOGGER.debug("Unregister service: {}", serviceState);
        for (String className : serviceState.getClassNames()) {
            try {
                List<ServiceState<?>> serviceStates = serviceMap.get(className);
                if (serviceStates != null) {
                    serviceStates.remove(serviceState);
                }
            } catch (RuntimeException ex) {
                LOGGER.error("Cannot unregister service: " + className, ex);
            }
        }

        Module serviceOwner = serviceState.getServiceOwner();

        // This event is synchronously delivered before the service has completed unregistering.
        frameworkEvents.fireServiceEvent(serviceOwner, ServiceEvent.UNREGISTERING, serviceState);

        // Remove from using modules
        for (AbstractModule module : serviceState.getUsingModulesInternal()) {
            while (ungetService(module, serviceState)) {
            }
        }
    }

    /**
     * Releases the service object referenced by the specified <code>ServiceReference</code> object. If the context module's use
     * count for the service is zero, this method returns <code>false</code>. Otherwise, the context module's use count for the
     * service is decremented by one.
     *
     * @return <code>false</code> if the context module's use count for the service is zero or if the service has been
     *         unregistered; <code>true</code> otherwise.
     */
    boolean ungetService(AbstractModule module, ServiceState<?> serviceState) {
        serviceState.ungetScopedValue(module);
        int useCount = module.removeServiceInUse(serviceState);
        if (useCount == 0) {
            serviceState.removeUsingModule(module);
        }
        return useCount >= 0;
    }
}
