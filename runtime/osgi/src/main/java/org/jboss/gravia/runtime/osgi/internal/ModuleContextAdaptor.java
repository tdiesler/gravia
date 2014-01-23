/*
 * #%L
 * Gravia :: Runtime :: OSGi
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
package org.jboss.gravia.runtime.osgi.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleEvent;
import org.jboss.gravia.runtime.ModuleListener;
import org.jboss.gravia.runtime.ServiceEvent;
import org.jboss.gravia.runtime.ServiceFactory;
import org.jboss.gravia.runtime.ServiceListener;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.SynchronousModuleListener;
import org.jboss.gravia.runtime.spi.AbstractModuleContext;
import org.jboss.gravia.utils.NotNullException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.SynchronousBundleListener;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
final class ModuleContextAdaptor extends AbstractModuleContext {

    private final BundleContext bundleContext;

    ModuleContextAdaptor(Module module, BundleContext bundleContext) {
        super(module);
        NotNullException.assertValue(bundleContext, "bundleContext");
        this.bundleContext = bundleContext;
    }

    @Override
    public void addModuleListener(ModuleListener listener) {
        bundleContext.addBundleListener(adaptModuleListener(listener));
    }

    @Override
    public void removeModuleListener(ModuleListener listener) {
        bundleContext.removeBundleListener(adaptModuleListener(listener));
    }

    @Override
    public void addServiceListener(ServiceListener listener, String filter) {
        try {
            bundleContext.addServiceListener(new ServiceListenerAdaptor(listener), filter);
        } catch (InvalidSyntaxException ex) {
            throw new IllegalArgumentException(filter, ex);
        }
    }

    @Override
    public void addServiceListener(ServiceListener listener) {
        bundleContext.addServiceListener(new ServiceListenerAdaptor(listener));
    }

    @Override
    public void removeServiceListener(ServiceListener listener) {
        bundleContext.removeServiceListener(new ServiceListenerAdaptor(listener));
    }

    @Override
    public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
        return new ServiceRegistrationAdaptor<S>(bundleContext.registerService(clazz, adaptServiceFactory(service), properties));
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ServiceRegistration<?> registerService(String className, Object service, Dictionary<String, ?> properties) {
        return new ServiceRegistrationAdaptor(bundleContext.registerService(className, adaptServiceFactory(service), properties));
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ServiceRegistration<?> registerService(String[] classNames, Object service, Dictionary<String, ?> properties) {
        return new ServiceRegistrationAdaptor(bundleContext.registerService(classNames, adaptServiceFactory(service), properties));
    }

    @Override
    public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
        org.osgi.framework.ServiceReference<S> sref = bundleContext.getServiceReference(clazz);
        return sref != null ? new ServiceReferenceAdaptor<S>(sref) : null;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ServiceReference<?> getServiceReference(String className) {
        org.osgi.framework.ServiceReference<?> sref = bundleContext.getServiceReference(className);
        return sref != null ? new ServiceReferenceAdaptor(sref) : null;
    }

    @Override
    public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter) {
        Collection<org.osgi.framework.ServiceReference<S>> srefs;
        try {
            srefs = bundleContext.getServiceReferences(clazz, filter);
        } catch (InvalidSyntaxException ex) {
            throw new IllegalArgumentException(filter, ex);
        }

        List<ServiceReference<S>> result = new ArrayList<ServiceReference<S>>();
        for (org.osgi.framework.ServiceReference<S> sref : srefs)
            result.add(new ServiceReferenceAdaptor<S>(sref));

        return Collections.unmodifiableList(result);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ServiceReference<?>[] getServiceReferences(String className, String filter) {
        org.osgi.framework.ServiceReference<?>[] srefs;
        try {
            srefs = bundleContext.getServiceReferences(className, filter);
        } catch (InvalidSyntaxException ex) {
            throw new IllegalArgumentException(filter, ex);
        }
        if (srefs == null)
            return null;

        List<ServiceReference<?>> result = new ArrayList<ServiceReference<?>>();
        for (org.osgi.framework.ServiceReference<?> sref : srefs)
            result.add(new ServiceReferenceAdaptor(sref));

        return result.toArray(new ServiceReference[result.size()]);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ServiceReference<?>[] getAllServiceReferences(String className, String filter) {
        org.osgi.framework.ServiceReference<?>[] srefs;
        try {
            srefs = bundleContext.getAllServiceReferences(className, filter);
        } catch (InvalidSyntaxException ex) {
            throw new IllegalArgumentException(filter, ex);
        }
        if (srefs == null)
            return null;

        List<ServiceReference<?>> result = new ArrayList<ServiceReference<?>>();
        for (org.osgi.framework.ServiceReference<?> sref : srefs)
            result.add(new ServiceReferenceAdaptor(sref));

        return result.toArray(new ServiceReference[result.size()]);
    }

    @Override
    public <S> S getService(ServiceReference<S> reference) {
        ServiceReferenceAdaptor<S> adaptor = (ServiceReferenceAdaptor<S>) reference;
        return bundleContext.getService(adaptor.delegate);
    }

    @Override
    public boolean ungetService(ServiceReference<?> reference) {
        ServiceReferenceAdaptor<?> adaptor = (ServiceReferenceAdaptor<?>) reference;
        return bundleContext.ungetService(adaptor.delegate);
    }

    private Module mappedModule(Bundle bundle) {
        OSGiRuntime runtime = (OSGiRuntime) getModule().adapt(Runtime.class);
        return runtime.getModule(bundle);
    }

    @SuppressWarnings("unchecked")
    private <S> S adaptServiceFactory(S service) {
        if (service instanceof ServiceFactory) {
            ServiceFactory<S> factory = (ServiceFactory<S>) service;
            service = (S) new ServiceFactoryAdaptor<S>(factory);
        }
        return service;
    }

    private BundleListener adaptModuleListener(ModuleListener listener) {
        if (listener instanceof SynchronousModuleListener) {
            return new SynchronousModuleListenerAdaptor(listener);
        } else {
            return new ModuleListenerAdaptor(listener);
        }
    }

    private class SynchronousModuleListenerAdaptor extends ModuleListenerAdaptor implements SynchronousBundleListener {

        SynchronousModuleListenerAdaptor(ModuleListener delegate) {
            super(delegate);
        }
    }

    private class ModuleListenerAdaptor implements BundleListener {

        private final ModuleListener delegate;

        ModuleListenerAdaptor(ModuleListener delegate) {
            NotNullException.assertValue(delegate, "delegate");
            this.delegate = delegate;
        }

        @Override
        public void bundleChanged(BundleEvent event) {
            int type = event.getType();
            Module module = mappedModule(event.getBundle());
            if (module != null) {
                ModuleEvent moduleEvent = new ModuleEvent(type, module);
                delegate.moduleChanged(moduleEvent);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof ModuleListenerAdaptor)) return false;
            ModuleListenerAdaptor other = (ModuleListenerAdaptor) obj;
            return delegate.equals(other.delegate);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }
    }

    private class ServiceListenerAdaptor implements org.osgi.framework.ServiceListener {

        private final ServiceListener delegate;

        ServiceListenerAdaptor(ServiceListener delegate) {
            NotNullException.assertValue(delegate, "delegate");
            this.delegate = delegate;
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public void serviceChanged(org.osgi.framework.ServiceEvent event) {
            ServiceReference<?> sref = new ServiceReferenceAdaptor(event.getServiceReference());
            delegate.serviceChanged(new ServiceEvent(event.getType(), sref));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof ServiceListenerAdaptor)) return false;
            ServiceListenerAdaptor other = (ServiceListenerAdaptor) obj;
            return delegate.equals(other.delegate);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }
    }

    private class ServiceReferenceAdaptor<S> implements ServiceReference<S> {

        private final org.osgi.framework.ServiceReference<S> delegate;

        ServiceReferenceAdaptor(org.osgi.framework.ServiceReference<S> delegate) {
            NotNullException.assertValue(delegate, "delegate");
            this.delegate = delegate;
        }

        @Override
        public Object getProperty(String key) {
            return delegate.getProperty(key);
        }

        @Override
        public String[] getPropertyKeys() {
            return delegate.getPropertyKeys();
        }

        @Override
        public Module getModule() {
            Bundle bundle = delegate.getBundle();
            return mappedModule(bundle);
        }

        @Override
        public boolean isAssignableTo(Module module, String className) {
            return delegate.isAssignableTo(module.adapt(Bundle.class), className);
        }

        @Override
        public int compareTo(Object reference) {
            return new ServiceReferenceAdaptor<S>(delegate).compareTo(reference);
        }
    }

    private class ServiceRegistrationAdaptor<S> implements ServiceRegistration<S> {

        private final org.osgi.framework.ServiceRegistration<S> delegate;

        ServiceRegistrationAdaptor(org.osgi.framework.ServiceRegistration<S> delegate) {
            NotNullException.assertValue(delegate, "delegate");
            this.delegate = delegate;
        }

        @Override
        public ServiceReference<S> getReference() {
            return new ServiceReferenceAdaptor<S>(delegate.getReference());
        }

        @Override
        public void setProperties(Dictionary<String, ?> properties) {
            delegate.setProperties(properties);
        }

        @Override
        public void unregister() {
            delegate.unregister();
        }
    }

    private class ServiceFactoryAdaptor<S> implements org.osgi.framework.ServiceFactory<S> {

        private final ServiceFactory<S> delegate;

        ServiceFactoryAdaptor(ServiceFactory<S> delegate) {
            NotNullException.assertValue(delegate, "delegate");
            this.delegate = delegate;
        }

        @Override
        public S getService(Bundle bundle, org.osgi.framework.ServiceRegistration<S> registration) {
            return delegate.getService(mappedModule(bundle), new ServiceRegistrationAdaptor<S>(registration));
        }

        @Override
        public void ungetService(Bundle bundle, org.osgi.framework.ServiceRegistration<S> registration, S service) {
            delegate.ungetService(mappedModule(bundle), new ServiceRegistrationAdaptor<S>(registration), service);
        }
    }
}
