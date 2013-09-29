/*
 * #%L
 * JBossOSGi Framework
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
package org.jboss.gravia.runtime.embedded;

import java.security.AccessControlContext;
import java.security.AccessController;
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
import java.util.concurrent.ExecutorService;

import org.jboss.gravia.runtime.AllServiceListener;
import org.jboss.gravia.runtime.ModuleEvent;
import org.jboss.gravia.runtime.ModuleListener;
import org.jboss.gravia.runtime.Constants;
import org.jboss.gravia.runtime.Filter;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.RuntimeUtils;
import org.jboss.gravia.runtime.ServiceEvent;
import org.jboss.gravia.runtime.ServiceListener;
import org.jboss.gravia.runtime.SynchronousModuleListener;
import org.jboss.gravia.runtime.embedded.osgi.ConstantsHelper;
import org.jboss.logging.Logger;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
final class EmbeddedRuntimeEventsHandler {

    private static Logger LOGGER = Logger.getLogger(EmbeddedRuntimeEventsHandler.class);

    private final ExecutorService executorService;

    /** The bundleState listeners */
    private final Map<Module, List<BundleListenerRegistration>> bundleListeners = new ConcurrentHashMap<Module, List<BundleListenerRegistration>>();
    /** The service listeners */
    private final Map<Module, List<ServiceListenerRegistration>> serviceListeners = new ConcurrentHashMap<Module, List<ServiceListenerRegistration>>();

    /** The set of bundleState events that are delivered to an (asynchronous) BundleListener */
    private Set<Integer> asyncBundleEvents = new HashSet<Integer>();
    /** The set of events that are logged at INFO level */
    private Set<String> infoEvents = new HashSet<String>();

    EmbeddedRuntimeEventsHandler(ExecutorService executorService) {
        this.executorService = executorService;
        asyncBundleEvents.add(new Integer(ModuleEvent.INSTALLED));
        asyncBundleEvents.add(new Integer(ModuleEvent.RESOLVED));
        asyncBundleEvents.add(new Integer(ModuleEvent.STARTED));
        asyncBundleEvents.add(new Integer(ModuleEvent.STOPPED));
        asyncBundleEvents.add(new Integer(ModuleEvent.UNRESOLVED));
        asyncBundleEvents.add(new Integer(ModuleEvent.UNINSTALLED));
        infoEvents.add(ConstantsHelper.bundleEvent(ModuleEvent.INSTALLED));
        infoEvents.add(ConstantsHelper.bundleEvent(ModuleEvent.STARTED));
        infoEvents.add(ConstantsHelper.bundleEvent(ModuleEvent.STOPPED));
        infoEvents.add(ConstantsHelper.bundleEvent(ModuleEvent.UNINSTALLED));
    }

    public void addBundleListener(final Module bundle, final ModuleListener listener) {
        assert listener != null : "Null listener";
        synchronized (bundleListeners) {
            List<BundleListenerRegistration> registrations = bundleListeners.get(bundle);
            if (registrations == null) {
                registrations = new ArrayList<BundleListenerRegistration>();
                bundleListeners.put(bundle, registrations);
            }
            BundleListenerRegistration registration = new BundleListenerRegistration(bundle, listener);
            if (registrations.contains(registration) == false) {
                registrations.add(registration);
            }
        }
    }


    public void removeBundleListener(final Module bundle, final ModuleListener listener) {
        assert listener != null : "Null listener";
        synchronized (bundleListeners) {
            List<BundleListenerRegistration> registrations = bundleListeners.get(bundle);
            if (registrations != null) {
                if (registrations.size() > 1) {
                    Iterator<BundleListenerRegistration> iterator = registrations.iterator();
                    while(iterator.hasNext()) {
                        BundleListenerRegistration registration = iterator.next();
                        if (registration.getListener() == listener) {
                            iterator.remove();
                            break;
                        }
                    }
                } else {
                    removeBundleListeners(bundle);
                }
            }
        }
    }


    public void removeBundleListeners(final Module bundleState) {
        synchronized (bundleListeners) {
            bundleListeners.remove(bundleState);
        }
    }


    public void removeAllBundleListeners() {
        synchronized (bundleListeners) {
            bundleListeners.clear();
        }
    }

    public void addServiceListener(final Module module, final ServiceListener listener, final String filterstr) {
        assert listener != null : "Null listener";
        synchronized (serviceListeners) {
            List<ServiceListenerRegistration> listeners = serviceListeners.get(module);
            if (listeners == null) {
                listeners = new ArrayList<ServiceListenerRegistration>();
                serviceListeners.put(module, listeners);
            }

            // If the context bundleState's list of listeners already contains a listener l such that (l==listener),
            // then this method replaces that listener's filter (which may be null) with the specified one (which may be null).
            removeServiceListener(module, listener);

            // Create the new listener registration
            Filter filter = (filterstr != null ? RuntimeUtils.createFilter(filterstr) : NoFilter.INSTANCE);
            ServiceListenerRegistration slreg = new ServiceListenerRegistration(module, listener, filter);

            // Add the listener to the list
            listeners.add(slreg);
        }
    }


    public Collection<ListenerInfo> getServiceListenerInfos(final Module bundleState) {
        Collection<ListenerInfo> listeners = new ArrayList<ListenerInfo>();
        for (Entry<Module, List<ServiceListenerRegistration>> entry : serviceListeners.entrySet()) {
            if (bundleState == null || bundleState.equals(entry.getKey())) {
                for (ServiceListenerRegistration aux : entry.getValue()) {
                    ListenerInfo info = aux.getListenerInfo();
                    listeners.add(info);
                }
            }
        }
        return Collections.unmodifiableCollection(listeners);
    }


    public void removeServiceListener(final Module bundleState, final ServiceListener listener) {
        assert listener != null : "Null listener";
        synchronized (serviceListeners) {
            List<ServiceListenerRegistration> listeners = serviceListeners.get(bundleState);
            if (listeners != null) {
                ServiceListenerRegistration slreg = new ServiceListenerRegistration(bundleState, listener, NoFilter.INSTANCE);
                int index = listeners.indexOf(slreg);
                if (index >= 0) {
                    slreg = listeners.remove(index);
                }
            }
        }
    }


    public void removeServiceListeners(final Module bundleState) {
        synchronized (serviceListeners) {
            serviceListeners.remove(bundleState);
        }
    }


    public void removeAllServiceListeners() {
        synchronized (serviceListeners) {
            serviceListeners.clear();
        }
    }


    public void fireBundleEvent(final Module bundle, final int type) {
        fireBundleEvent(null, bundle, type);
    }


    public void fireBundleEvent(final ModuleContext context, final Module bundle, final int type) {
        if (bundle == null)
            throw new IllegalArgumentException("bundle");

        // Do nothing it the framework is not active
        // if (bundleManager.isFrameworkCreated() == false)
        //    return;

        // Get a snapshot of the current listeners
        final List<BundleListenerRegistration> registrations = new ArrayList<BundleListenerRegistration>();
        synchronized (bundleListeners) {
            for (Entry<Module, List<BundleListenerRegistration>> entry : bundleListeners.entrySet()) {
                for (BundleListenerRegistration blreg : entry.getValue()) {
                    registrations.add(blreg);
                }
            }
        }

        // Expose the bundleState wrapper not the state itself
        final ModuleEvent event = new BundleEventImpl(type, bundle, context != null ? context.getModule() : bundle);
        final String typeName = ConstantsHelper.bundleEvent(event.getType());

        // Nobody is interested
        Iterator<BundleListenerRegistration> iterator = registrations.iterator();
        if (registrations.isEmpty())
            return;

        // Synchronous listeners first
        iterator = registrations.iterator();
        while (iterator.hasNext()) {
            BundleListenerRegistration blreg = iterator.next();
            ModuleListener listener = blreg.listener;
            try {
                if (listener instanceof SynchronousModuleListener) {
                    iterator.remove();
                    listener.moduleChanged(event);
                }
            } catch (Throwable th) {
                LOGGER.warnf(th, "Error while firing bundle event %s for: %s", typeName, bundle);
            }
        }

        if (!registrations.isEmpty()) {
            Runnable runner = new Runnable() {

                @Override
                public void run() {
                    // BundleListeners are called with a BundleEvent object when a bundleState has been
                    // installed, resolved, started, stopped, updated, unresolved, or uninstalled
                    if (asyncBundleEvents.contains(type)) {
                        for (BundleListenerRegistration blreg : registrations) {
                            ModuleListener listener = blreg.listener;
                            try {
                                if (!(listener instanceof SynchronousModuleListener)) {
                                    listener.moduleChanged(event);
                                }
                            } catch (Throwable th) {
                                LOGGER.warnf(th, "Error while firing bundle event %s for: %s", typeName, bundle);
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

    public void fireServiceEvent(final Module bundle, int type, final ServiceState<?> serviceState) {

        // Do nothing it the framework is not active
        // if (bundleManager.isFrameworkCreated() == false)
        //    return;

        // Get a snapshot of the current listeners
        Map<ModuleContext, Collection<ListenerInfo>> listeners;
        synchronized (serviceListeners) {
            listeners = new HashMap<ModuleContext, Collection<ListenerInfo>>();
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
        }

        // Construct the ServiceEvent
        ServiceEvent event = new ServiceEventImpl(type, serviceState);
        String typeName = ConstantsHelper.serviceEvent(event.getType());
        LOGGER.tracef("Service %s: %s", typeName, serviceState);

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
                    String[] clazzes = (String[]) serviceState.getProperty(Constants.OBJECTCLASS);
                    for (String clazz : clazzes) {
                        if (serviceState.isAssignableTo(owner, clazz) == false) {
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
                    if (listenerReg.isAllServiceListener() || listenerReg.filter.match(serviceState)) {
                        listener.serviceChanged(event);
                    }

                    // The MODIFIED_ENDMATCH event is synchronously delivered after the service properties have been modified.
                    // This event is only delivered to listeners which were added with a non-null filter where
                    // the filter matched the service properties prior to the modification but the filter does
                    // not match the modified service properties.
                    else if (filterstr != null && ServiceEvent.MODIFIED == event.getType()) {
                        if (listenerReg.filter.match(serviceState.getPreviousProperties())) {
                            event = new ServiceEventImpl(ServiceEvent.MODIFIED_ENDMATCH, serviceState);
                            listener.serviceChanged(event);
                        }
                    }
                } catch (Throwable th) {
                    LOGGER.warnf(th, "Error while firing service event %s for: %s", typeName, serviceState);
                }
            }
        }
    }

    /**
     * Filter and AccessControl for service events
     */
    static class ServiceListenerRegistration {

        private final Module module;
        private final ModuleContext bundleContext;
        private final ServiceListener listener;
        private final Filter filter;
        private final ListenerInfo info;

        // Any access control context
        AccessControlContext accessControlContext;

        ServiceListenerRegistration(final Module module, final ServiceListener listener, final Filter filter) {
            assert module != null : "Null bundle";
            assert listener != null : "Null listener";
            assert filter != null : "Null filter";
            this.module = module;
            this.listener = listener;
            this.filter = filter;
            this.bundleContext = module.getModuleContext();
            this.info = new ListenerInfo(bundleContext, this);
            if (System.getSecurityManager() != null)
                accessControlContext = AccessController.getContext();
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

    static class BundleListenerRegistration {
        private final ModuleListener listener;
        private final ModuleContext bundleContext;
        private final Module bundle;

        BundleListenerRegistration(Module bundle, ModuleListener listener) {
            this.listener = listener;
            this.bundle = bundle;
            this.bundleContext = bundle.getModuleContext();
        }

        ModuleListener getListener() {
            return listener;
        }

        Module getModule() {
            return bundle;
        }

        ModuleContext getModuleContext() {
            return bundleContext;
        }


        @Override
        public int hashCode() {
            return listener.hashCode();
        }


        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BundleListenerRegistration == false)
                return false;

            // Only the BundleListener instance determins equality
            BundleListenerRegistration other = (BundleListenerRegistration) obj;
            return other.listener.equals(listener);
        }


        @Override
        public String toString() {
            String className = listener.getClass().getName();
            return "BundleListener[" + bundle + "," + className + "]";
        }
    }

    static class ListenerInfo {

        private final ServiceListenerRegistration registration;
        private final ModuleContext moduleContext;
        private boolean removed;

        ListenerInfo(ModuleContext bundleContext, ServiceListenerRegistration registration) {
            this.moduleContext = bundleContext;
            this.registration = registration;
        }

        public ModuleContext getModuleContext() {
            return moduleContext;
        }

        public String getFilter() {
            Filter filter = registration.filter;
            return filter != NoFilter.INSTANCE ? filter.toString() : null;
        }

        public boolean isRemoved() {
            return removed;
        }

        ServiceListenerRegistration getRegistration() {
            return registration;
        }

        void setRemoved(boolean removed) {
            this.removed = removed;
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

    static class BundleEventImpl extends ModuleEvent {

        private static final long serialVersionUID = -2705304702665185935L;

        BundleEventImpl(int type, Module bundle, Module origin) {
            super(type, bundle, origin);
        }


        @Override
        public String toString() {
            return "BundleEvent[type=" + ConstantsHelper.bundleEvent(getType()) + ",source=" + getSource() + "]";
        }
    }

    static class ServiceEventImpl extends ServiceEvent {

        private static final long serialVersionUID = 62018288275708239L;

        public ServiceEventImpl(int type, ServiceState<?> serviceState) {
            super(type, serviceState.getReference());
        }


        @Override
        public String toString() {
            return "ServiceEvent[type=" + ConstantsHelper.serviceEvent(getType()) + ",source=" + getSource() + "]";
        }
    }
}
