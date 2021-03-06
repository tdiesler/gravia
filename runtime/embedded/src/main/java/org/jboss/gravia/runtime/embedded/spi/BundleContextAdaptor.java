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
package org.jboss.gravia.runtime.embedded.spi;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.List;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleEvent;
import org.jboss.gravia.runtime.ModuleListener;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.SynchronousModuleListener;
import org.jboss.gravia.utils.IllegalArgumentAssertion;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.SynchronousBundleListener;

/**
 * Bundle implementation that delegates all functionality to
 * the underlying Module.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public final class BundleContextAdaptor implements BundleContext {

    private final ModuleContext moduleContext;

    public BundleContextAdaptor(ModuleContext moduleContext) {
        IllegalArgumentAssertion.assertNotNull(moduleContext, "moduleContext");
        this.moduleContext = moduleContext;
    }

    // BundleContext API

    @Override
    public Bundle getBundle() {
        Module module = moduleContext.getModule();
        return new BundleAdaptor(module);
    }

    @Override
    public String getProperty(String key) {
        Object value = getRuntime().getProperty(key);
        return (value instanceof String ? (String) value : null);
    }

    @Override
    public Bundle getBundle(long id) {
        Module module = getRuntime().getModule(id);
        return module != null ? new BundleAdaptor(module) : null;
    }

    @Override
    public Bundle installBundle(String location, InputStream input) throws BundleException {
        throw new UnsupportedOperationException("BundleContext.installBundle(String,InputStream)");
    }

    @Override
    public Bundle installBundle(String location) throws BundleException {
        throw new UnsupportedOperationException("BundleContext.installBundle(String)");
    }

    @Override
    public Bundle getBundle(String location) {
        throw new UnsupportedOperationException("BundleContext.getBundle(String)");
    }

    @Override
    public Bundle[] getBundles() {
        List<Bundle> bundles = new ArrayList<Bundle>();
        for (Module module : getRuntime().getModules()) {
            bundles.add(new BundleAdaptor(module));
        }
        return bundles.toArray(new Bundle[bundles.size()]);
    }

    @Override
    public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {
        moduleContext.addServiceListener(new ServiceListenerAdaptor(listener), filter);
    }

    @Override
    public void addServiceListener(ServiceListener listener) {
        moduleContext.addServiceListener(new ServiceListenerAdaptor(listener));
    }

    @Override
    public void removeServiceListener(ServiceListener listener) {
        moduleContext.removeServiceListener(new ServiceListenerAdaptor(listener));
    }

    @Override
    public void addBundleListener(BundleListener listener) {
        moduleContext.addModuleListener(adaptBundleListener(listener));
    }

    @Override
    public void removeBundleListener(BundleListener listener) {
        moduleContext.removeModuleListener(new BundleListenerAdaptor(listener));
    }

    @Override
    public void addFrameworkListener(FrameworkListener listener) {
        throw new UnsupportedOperationException("BundleContext.addFrameworkListener(FrameworkListener)");
    }

    @Override
    public void removeFrameworkListener(FrameworkListener listener) {
        throw new UnsupportedOperationException("BundleContext.removeFrameworkListener(FrameworkListener)");
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ServiceRegistration<?> registerService(String[] classNames, Object service, Dictionary<String, ?> properties) {
        return new ServiceRegistrationAdaptor(moduleContext.registerService(classNames, adaptServiceFactory(service), properties));
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ServiceRegistration<?> registerService(String className, Object service, Dictionary<String, ?> properties) {
        return new ServiceRegistrationAdaptor(moduleContext.registerService(className, adaptServiceFactory(service), properties));
    }

    @Override
    public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
        return new ServiceRegistrationAdaptor<S>(moduleContext.registerService(clazz, adaptServiceFactory(service), properties));
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ServiceReference<?>[] getServiceReferences(String className, String filter) throws InvalidSyntaxException {
        org.jboss.gravia.runtime.ServiceReference<?>[] srefs = moduleContext.getServiceReferences(className, filter);
        if (srefs == null)
            return null;

        List<ServiceReference<?>> result = new ArrayList<ServiceReference<?>>();
        for (org.jboss.gravia.runtime.ServiceReference<?> sref : srefs)
            result.add(new ServiceReferenceAdaptor(sref));

        return result.toArray(new ServiceReference[result.size()]);
    }

    @Override
    public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter) throws InvalidSyntaxException {
        Collection<org.jboss.gravia.runtime.ServiceReference<S>> srefs = moduleContext.getServiceReferences(clazz, filter);

        List<ServiceReference<S>> result = new ArrayList<ServiceReference<S>>();
        for (org.jboss.gravia.runtime.ServiceReference<S> sref : srefs)
            result.add(new ServiceReferenceAdaptor<S>(sref));

        return Collections.unmodifiableList(result);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ServiceReference<?>[] getAllServiceReferences(String className, String filter) throws InvalidSyntaxException {
        org.jboss.gravia.runtime.ServiceReference<?>[] srefs = moduleContext.getAllServiceReferences(className, filter);
        if (srefs == null)
            return null;

        List<ServiceReference<?>> result = new ArrayList<ServiceReference<?>>();
        for (org.jboss.gravia.runtime.ServiceReference<?> sref : srefs)
            result.add(new ServiceReferenceAdaptor(sref));

        return result.toArray(new ServiceReference[result.size()]);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ServiceReference<?> getServiceReference(String className) {
        org.jboss.gravia.runtime.ServiceReference<?> sref = moduleContext.getServiceReference(className);
        return sref != null ? new ServiceReferenceAdaptor(sref) : null;
    }

    @Override
    public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
        org.jboss.gravia.runtime.ServiceReference<S> sref = moduleContext.getServiceReference(clazz);
        return sref != null ? new ServiceReferenceAdaptor<S>(sref) : null;
    }

    @Override
    public <S> S getService(ServiceReference<S> reference) {
        ServiceReferenceAdaptor<S> adaptor = (ServiceReferenceAdaptor<S>) reference;
        return moduleContext.getService(adaptor.delegate);
    }

    @Override
    public boolean ungetService(ServiceReference<?> reference) {
        ServiceReferenceAdaptor<?> adaptor = (ServiceReferenceAdaptor<?>) reference;
        return moduleContext.ungetService(adaptor.delegate);
    }

    @Override
    public File getDataFile(String filename) {
        return moduleContext.getModule().getDataFile(filename);
    }

    @Override
    public Filter createFilter(String filter) throws InvalidSyntaxException {
        return FrameworkUtil.createFilter(filter);
    }

    private Runtime getRuntime() {
        return moduleContext.getModule().adapt(Runtime.class);
    }

    @SuppressWarnings("unchecked")
    private <S> S adaptServiceFactory(S service) {
        if (service instanceof ServiceFactory) {
            ServiceFactory<S> factory = (ServiceFactory<S>) service;
            service = (S) new ServiceFactoryAdaptor<S>(factory);
        }
        return service;
    }

    private ModuleListener adaptBundleListener(BundleListener listener) {
        if (listener instanceof SynchronousBundleListener) {
            return new SynchronousBundleListenerAdaptor(listener);
        } else {
            return new BundleListenerAdaptor(listener);
        }
    }

    @Override
    public String toString() {
        return "BundleContext[" + moduleContext.getModule().getIdentity() + "]";
    }

    private class SynchronousBundleListenerAdaptor extends BundleListenerAdaptor implements SynchronousModuleListener {

        SynchronousBundleListenerAdaptor(BundleListener delegate) {
            super(delegate);
        }
    }

    private class BundleListenerAdaptor implements ModuleListener {

        private final BundleListener delegate;

        BundleListenerAdaptor(BundleListener delegate) {
            IllegalArgumentAssertion.assertNotNull(delegate, "delegate");
            this.delegate = delegate;
        }

        @Override
        public void moduleChanged(ModuleEvent event) {
            int type = event.getType();
            Module module = event.getModule();
            BundleEvent bundleEvent = new BundleEvent(type, new BundleAdaptor(module));
            delegate.bundleChanged(bundleEvent);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof BundleListenerAdaptor)) return false;
            BundleListenerAdaptor other = (BundleListenerAdaptor) obj;
            return delegate.equals(other.delegate);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }

    private class ServiceListenerAdaptor implements org.jboss.gravia.runtime.ServiceListener {

        private final ServiceListener delegate;

        ServiceListenerAdaptor(ServiceListener delegate) {
            IllegalArgumentAssertion.assertNotNull(delegate, "delegate");
            this.delegate = delegate;
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public void serviceChanged(org.jboss.gravia.runtime.ServiceEvent event) {
            ServiceReference<?> sref = new ServiceReferenceAdaptor(event.getServiceReference());
            delegate.serviceChanged(new ServiceEvent(event.getType(), sref));
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof ServiceListenerAdaptor)) return false;
            ServiceListenerAdaptor other = (ServiceListenerAdaptor) obj;
            return delegate.equals(other.delegate);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }

    private class ServiceReferenceAdaptor<S> implements ServiceReference<S> {

        private final org.jboss.gravia.runtime.ServiceReference<S> delegate;

        ServiceReferenceAdaptor(org.jboss.gravia.runtime.ServiceReference<S> delegate) {
            IllegalArgumentAssertion.assertNotNull(delegate, "delegate");
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
        public Bundle getBundle() {
            Module module = delegate.getModule();
            return new BundleAdaptor(module);
        }

        @Override
        public Bundle[] getUsingBundles() {
            throw new UnsupportedOperationException("BundleContext.getUsingBundles()");
        }

        @Override
        public boolean isAssignableTo(Bundle bundle, String className) {
            Module module = mappedModule(bundle);
            return delegate.isAssignableTo(module, className);
        }

        @Override
        public int compareTo(Object sref) {
            if (!(sref instanceof ServiceReference))
                throw new IllegalArgumentException("Invalid ServiceReference: " + sref);

            Comparator<ServiceReference<?>> comparator = ServiceReferenceComparator.getInstance();
            return comparator.compare(this, (ServiceReference<?>) sref);
        }

        private Module mappedModule(Bundle bundle) {
            Module result = null;
            if (bundle != null) {
                Runtime runtime = RuntimeLocator.getRequiredRuntime();
                result = runtime.getModule(bundle.getBundleId());
            }
            return result;
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof ServiceReferenceAdaptor)) return false;
            ServiceReferenceAdaptor<?> other = (ServiceReferenceAdaptor<?>) obj;
            return delegate.equals(other.delegate);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }

    private class ServiceRegistrationAdaptor<S> implements ServiceRegistration<S> {

        private final org.jboss.gravia.runtime.ServiceRegistration<S> delegate;

        ServiceRegistrationAdaptor(org.jboss.gravia.runtime.ServiceRegistration<S> delegate) {
            IllegalArgumentAssertion.assertNotNull(delegate, "delegate");
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

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof ServiceRegistrationAdaptor)) return false;
            ServiceRegistrationAdaptor<?> other = (ServiceRegistrationAdaptor<?>) obj;
            return delegate.equals(other.delegate);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }

    private class ServiceFactoryAdaptor<S> implements org.jboss.gravia.runtime.ServiceFactory<S> {

        private final ServiceFactory<S> delegate;

        ServiceFactoryAdaptor(ServiceFactory<S> delegate) {
            IllegalArgumentAssertion.assertNotNull(delegate, "delegate");
            this.delegate = delegate;
        }

        @Override
        public S getService(Module module, org.jboss.gravia.runtime.ServiceRegistration<S> registration) {
            return delegate.getService(new BundleAdaptor(module), new ServiceRegistrationAdaptor<S>(registration));
        }

        @Override
        public void ungetService(Module module, org.jboss.gravia.runtime.ServiceRegistration<S> registration, S service) {
            delegate.ungetService(new BundleAdaptor(module), new ServiceRegistrationAdaptor<S>(registration), service);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof ServiceFactoryAdaptor)) return false;
            ServiceFactoryAdaptor<?> other = (ServiceFactoryAdaptor<?>) obj;
            return delegate.equals(other.delegate);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }
}
