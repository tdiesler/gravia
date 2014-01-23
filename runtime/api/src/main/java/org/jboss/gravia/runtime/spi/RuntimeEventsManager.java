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
package org.jboss.gravia.runtime.spi;

import static org.jboss.gravia.runtime.spi.RuntimeLogger.LOGGER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jboss.gravia.runtime.AllServiceListener;
import org.jboss.gravia.runtime.ConstantsHelper;
import org.jboss.gravia.runtime.Filter;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleEvent;
import org.jboss.gravia.runtime.ModuleListener;
import org.jboss.gravia.runtime.ServiceEvent;
import org.jboss.gravia.runtime.ServiceListener;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.SynchronousModuleListener;
import org.jboss.gravia.utils.RemoveOnlyCollection;
import org.jboss.gravia.utils.RemoveOnlyMap;

/**
 * A manager for runtime listerners and their associated event delivery.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @ThreadSafe
 */
public final class RuntimeEventsManager {

    private final ExecutorService executorService;

    /** The moduleState listeners */
    private final Map<Module, List<ModuleListenerRegistration>> moduleListeners = new ConcurrentHashMap<Module, List<ModuleListenerRegistration>>();
    /** The service listeners */
    private final Map<Module, List<ServiceListenerRegistration>> serviceListeners = new ConcurrentHashMap<Module, List<ServiceListenerRegistration>>();

    /** The set of moduleState events that are delivered to an (asynchronous) BundleListener */
    private Set<Integer> asyncBundleEvents = new HashSet<Integer>();
    /** The set of events that are logged at INFO level */
    private Set<String> infoEvents = new HashSet<String>();

    RuntimeEventsManager() {
        asyncBundleEvents.add(new Integer(ModuleEvent.INSTALLED));
        asyncBundleEvents.add(new Integer(ModuleEvent.STARTED));
        asyncBundleEvents.add(new Integer(ModuleEvent.STOPPED));
        asyncBundleEvents.add(new Integer(ModuleEvent.UNINSTALLED));
        infoEvents.add(ConstantsHelper.moduleEvent(ModuleEvent.INSTALLED));
        infoEvents.add(ConstantsHelper.moduleEvent(ModuleEvent.STARTED));
        infoEvents.add(ConstantsHelper.moduleEvent(ModuleEvent.STOPPED));
        infoEvents.add(ConstantsHelper.moduleEvent(ModuleEvent.UNINSTALLED));
        executorService = createExecutorService("RuntimeEvents");
    }

    private ExecutorService createExecutorService(final String threadName) {
        ExecutorService service = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable run) {
                Thread thread = new Thread(run);
                thread.setName(threadName);
                return thread;
            }
        });
        return service;
    }

    public void addModuleListener(final Module module, final ModuleListener listener) {
        assert listener != null : "Null listener";
        synchronized (moduleListeners) {
            List<ModuleListenerRegistration> registrations = moduleListeners.get(module);
            if (registrations == null) {
                registrations = new CopyOnWriteArrayList<ModuleListenerRegistration>();
                moduleListeners.put(module, registrations);
            }
            ModuleListenerRegistration registration = new ModuleListenerRegistration(module, listener);
            if (registrations.contains(registration) == false) {
                registrations.add(registration);
            }
        }
    }


    public void removeModuleListener(final Module module, final ModuleListener listener) {
        assert listener != null : "Null listener";
        List<ModuleListenerRegistration> registrations = moduleListeners.get(module);
        if (registrations != null) {
            if (registrations.size() > 1) {
                Iterator<ModuleListenerRegistration> iterator = registrations.iterator();
                while(iterator.hasNext()) {
                    ModuleListenerRegistration registration = iterator.next();
                    if (registration.getListener() == listener) {
                        iterator.remove();
                        break;
                    }
                }
            } else {
                removeBundleListeners(module);
            }
        }
    }


    public void removeBundleListeners(final Module moduleState) {
        synchronized (moduleListeners) {
            moduleListeners.remove(moduleState);
        }
    }


    public void removeAllBundleListeners() {
        synchronized (moduleListeners) {
            moduleListeners.clear();
        }
    }

    public void addServiceListener(final Module module, final ServiceListener listener, final String filterstr) {
        assert listener != null : "Null listener";
        synchronized (serviceListeners) {
            List<ServiceListenerRegistration> listeners = serviceListeners.get(module);
            if (listeners == null) {
                listeners = new CopyOnWriteArrayList<ServiceListenerRegistration>();
                serviceListeners.put(module, listeners);
            }

            // If the context moduleState's list of listeners already contains a listener l such that (l==listener),
            // then this method replaces that listener's filter (which may be null) with the specified one (which may be null).
            removeServiceListener(module, listener);

            // Create the new listener registration
            Filter filter = (filterstr != null ? FilterFactory.createFilter(filterstr) : NoFilter.INSTANCE);
            ServiceListenerRegistration slreg = new ServiceListenerRegistration(module, listener, filter);

            // Add the listener to the list
            listeners.add(slreg);
        }
    }


    Collection<ListenerInfo> getServiceListenerInfos(final Module moduleState) {
        Collection<ListenerInfo> listeners = new ArrayList<ListenerInfo>();
        for (Entry<Module, List<ServiceListenerRegistration>> entry : serviceListeners.entrySet()) {
            if (moduleState == null || moduleState.equals(entry.getKey())) {
                for (ServiceListenerRegistration aux : entry.getValue()) {
                    ListenerInfo info = aux.getListenerInfo();
                    listeners.add(info);
                }
            }
        }
        return Collections.unmodifiableCollection(listeners);
    }


    public void removeServiceListener(final Module moduleState, final ServiceListener listener) {
        assert listener != null : "Null listener";
        List<ServiceListenerRegistration> listeners = serviceListeners.get(moduleState);
        if (listeners != null) {
            ServiceListenerRegistration slreg = new ServiceListenerRegistration(moduleState, listener, NoFilter.INSTANCE);
            int index = listeners.indexOf(slreg);
            if (index >= 0) {
                slreg = listeners.remove(index);
            }
        }
    }


    public void removeServiceListeners(final Module moduleState) {
        synchronized (serviceListeners) {
            serviceListeners.remove(moduleState);
        }
    }


    public void removeAllServiceListeners() {
        synchronized (serviceListeners) {
            serviceListeners.clear();
        }
    }


    public void fireModuleEvent(final Module module, final int type) {
        fireModuleEvent(null, module, type);
    }


    public void fireModuleEvent(final ModuleContext context, final Module module, final int type) {
        if (module == null)
            throw new IllegalArgumentException("module");

        // Do nothing it the framework is not active
        // if (moduleManager.isFrameworkCreated() == false)
        //    return;

        // Get a snapshot of the current listeners
        final List<ModuleListenerRegistration> registrations = new ArrayList<ModuleListenerRegistration>();
        for (Entry<Module, List<ModuleListenerRegistration>> entry : moduleListeners.entrySet()) {
            for (ModuleListenerRegistration blreg : entry.getValue()) {
                registrations.add(blreg);
            }
        }

        // Expose the moduleState wrapper not the state itself
        final ModuleEvent event = new ModuleEventImpl(type, module);
        final String typeName = ConstantsHelper.moduleEvent(event.getType());

        // Nobody is interested
        Iterator<ModuleListenerRegistration> iterator = registrations.iterator();
        if (registrations.isEmpty())
            return;

        // Synchronous listeners first
        iterator = registrations.iterator();
        while (iterator.hasNext()) {
            ModuleListenerRegistration blreg = iterator.next();
            ModuleListener listener = blreg.listener;
            try {
                if (listener instanceof SynchronousModuleListener) {
                    iterator.remove();
                    listener.moduleChanged(event);
                }
            } catch (Throwable th) {
                LOGGER.warn("Error while firing module event " + typeName + " for: " + module, th);
            }
        }

        if (!registrations.isEmpty()) {
            Runnable runner = new Runnable() {

                @Override
                public void run() {
                    // BundleListeners are called with a BundleEvent object when a moduleState has been
                    // installed, resolved, started, stopped, updated, unresolved, or uninstalled
                    if (asyncBundleEvents.contains(type)) {
                        for (ModuleListenerRegistration blreg : registrations) {
                            ModuleListener listener = blreg.listener;
                            try {
                                if (!(listener instanceof SynchronousModuleListener)) {
                                    listener.moduleChanged(event);
                                }
                            } catch (Throwable th) {
                                LOGGER.warn("Error while firing module event " + typeName + " for: " + module, th);
                            }
                        }
                    }
                }
            };
            if (!executorService.isShutdown()) {
                executorService.execute(runner);
            }
        }
    }

    public void fireServiceEvent(final Module module, int type, final ServiceReference<?> reference) {

        // Do nothing it the framework is not active
        // if (moduleManager.isFrameworkCreated() == false)
        //    return;

        // Get a snapshot of the current listeners
        Map<ModuleContext, Collection<ListenerInfo>> listeners = new HashMap<ModuleContext, Collection<ListenerInfo>>();
        for (Entry<Module, List<ServiceListenerRegistration>> entry : serviceListeners.entrySet()) {
            for (ServiceListenerRegistration listener : entry.getValue()) {
                ModuleContext context = listener.getModuleContext();
                if (context != null) {
                    Collection<ListenerInfo> infos = listeners.get(context);
                    if (infos == null) {
                        infos = new ArrayList<ListenerInfo>();
                        listeners.put(context, infos);
                    }
                    infos.add(listener.getListenerInfo());
                }
            }
        }
        for (Map.Entry<ModuleContext, Collection<ListenerInfo>> entry : listeners.entrySet()) {
            listeners.put(entry.getKey(), new RemoveOnlyCollection<ListenerInfo>(entry.getValue()));
        }
        listeners = new RemoveOnlyMap<ModuleContext, Collection<ListenerInfo>>(listeners);

        // Construct the ServiceEvent
        ServiceEvent event = new ServiceEventImpl(type, reference);
        String typeName = ConstantsHelper.serviceEvent(event.getType());
        LOGGER.trace("Service {}: {}", typeName, reference);

        // Nobody is interested
        if (listeners.isEmpty())
            return;

        // Call the listeners. All service events are synchronously delivered
        for (Map.Entry<ModuleContext, Collection<ListenerInfo>> entry : listeners.entrySet()) {
            for (ListenerInfo info : entry.getValue()) {
                ServiceListenerRegistration listenerReg = info.getRegistration();
                Module owner = info.getModuleContext().getModule();
                if (owner.getState() == Module.State.UNINSTALLED) {
                    continue;
                }
                // Service events must only be delivered to event listeners which can validly cast the event
                if (!listenerReg.isAllServiceListener()) {
                    boolean assignableToOwner = true;
                    String[] clazzes = (String[]) reference.getProperty(org.jboss.gravia.Constants.OBJECTCLASS);
                    for (String clazz : clazzes) {
                        if (reference.isAssignableTo(owner, clazz) == false) {
                            assignableToOwner = false;
                            break;
                        }
                    }
                    if (assignableToOwner == false)
                        continue;
                }

                try {
                    String filterstr = info.getFilter();
                    ServiceListener listener = listenerReg.getListener();
                    if (listenerReg.isAllServiceListener() || listenerReg.filter.match(reference)) {
                        listener.serviceChanged(event);
                    }

                    // The MODIFIED_ENDMATCH event is synchronously delivered after the service properties have been modified.
                    // This event is only delivered to listeners which were added with a non-null filter where
                    // the filter matched the service properties prior to the modification but the filter does
                    // not match the modified service properties.
                    else if (filterstr != null && ServiceEvent.MODIFIED == event.getType()) {
                        Filter filter = listenerReg.filter;
                        if (/* filter.match(reference.getPreviousProperties()) && */ !filter.match(reference)) {
                            ServiceEvent endmatch = new ServiceEventImpl(ServiceEvent.MODIFIED_ENDMATCH, reference);
                            listener.serviceChanged(endmatch);
                        }
                    }
                } catch (Throwable th) {
                    LOGGER.warn("Error while firing service event " + typeName + " for: " + reference, th);
                }
            }
        }
    }

    /**
     * Filter and AccessControl for service events
     */
    private static class ServiceListenerRegistration {

        private final Module module;
        private final ModuleContext moduleContext;
        private final ServiceListener listener;
        private final Filter filter;
        private final ListenerInfo info;

        ServiceListenerRegistration(final Module module, final ServiceListener listener, final Filter filter) {
            assert module != null : "Null module";
            assert listener != null : "Null listener";
            assert filter != null : "Null filter";
            this.module = module;
            this.listener = listener;
            this.filter = filter;
            this.moduleContext = module.getModuleContext();
            this.info = new ListenerInfo(moduleContext, this);
        }

        Module getModule() {
            return module;
        }

        ModuleContext getModuleContext() {
            return getModule().getModuleContext();
        }

        ServiceListener getListener() {
            return listener;
        }

        ListenerInfo getListenerInfo() {
            return info;
        }

        boolean isAllServiceListener() {
            return (listener instanceof AllServiceListener);
        }


        @Override
        public int hashCode() {
            return listener.hashCode();
        }


        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ServiceListenerRegistration == false)
                return false;

            // Only the ServiceListener instance determins equality
            ServiceListenerRegistration other = (ServiceListenerRegistration) obj;
            return other.listener.equals(listener);
        }


        @Override
        public String toString() {
            String className = listener.getClass().getName();
            return "ServiceListener[" + module + "," + className + "," + filter + "]";
        }
    }

    private static class ModuleListenerRegistration {
        private final ModuleListener listener;
        private final Module module;

        ModuleListenerRegistration(Module module, ModuleListener listener) {
            this.listener = listener;
            this.module = module;
        }

        ModuleListener getListener() {
            return listener;
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }


        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ModuleListenerRegistration == false)
                return false;

            // Only the BundleListener instance determins equality
            ModuleListenerRegistration other = (ModuleListenerRegistration) obj;
            return other.listener.equals(listener);
        }


        @Override
        public String toString() {
            String className = listener.getClass().getName();
            return "BundleListener[" + module + "," + className + "]";
        }
    }

    private static class ListenerInfo {

        private final ServiceListenerRegistration registration;
        private final ModuleContext moduleContext;
        private boolean removed;

        ListenerInfo(ModuleContext moduleContext, ServiceListenerRegistration registration) {
            this.moduleContext = moduleContext;
            this.registration = registration;
        }

        ModuleContext getModuleContext() {
            return moduleContext;
        }

        String getFilter() {
            Filter filter = registration.filter;
            return filter != NoFilter.INSTANCE ? filter.toString() : null;
        }

        ServiceListenerRegistration getRegistration() {
            return registration;
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }


        @Override
        public boolean equals(Object obj) {
            // Two ListenerInfos are equals if they refer to the same listener for a given addition and removal life cycle.
            // If the same listener is added again, it must have a different ListenerInfo which is not equal to this ListenerInfo.
            return super.equals(obj);
        }


        @Override
        public String toString() {
            String className = registration.listener.getClass().getName();
            return "ListenerInfo[" + moduleContext.getModule() + "," + className + "," + removed + "]";
        }
    }

    private static class ModuleEventImpl extends ModuleEvent {

        private static final long serialVersionUID = -2705304702665185935L;

        ModuleEventImpl(int type, Module module) {
            super(type, module);
        }


        @Override
        public String toString() {
            return "BundleEvent[type=" + ConstantsHelper.moduleEvent(getType()) + ",source=" + getSource() + "]";
        }
    }

    private static class ServiceEventImpl extends ServiceEvent {

        private static final long serialVersionUID = 62018288275708239L;

        ServiceEventImpl(int type, ServiceReference<?> reference) {
            super(type, reference);
        }


        @Override
        public String toString() {
            return "ServiceEvent[type=" + ConstantsHelper.serviceEvent(getType()) + ",source=" + getSource() + "]";
        }
    }
}
