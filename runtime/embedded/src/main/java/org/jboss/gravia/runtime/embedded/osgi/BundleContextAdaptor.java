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
package org.jboss.gravia.runtime.embedded.osgi;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Dictionary;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

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
        this.moduleContext = moduleContext;
    }

    // BundleContext API

    @Override
    public Bundle getBundle() {
        Module module = moduleContext.getModule();
        return new BundleAdaptor(module);
    }

    // Unsupported BundleContext API

    @Override
    public String getProperty(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle installBundle(String location, InputStream input) throws BundleException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle installBundle(String location) throws BundleException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getBundle(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle[] getBundles() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void addBundleListener(BundleListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeBundleListener(BundleListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addFrameworkListener(FrameworkListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeFrameworkListener(FrameworkListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServiceRegistration<?> registerService(String[] clazzes, Object service, Dictionary<String, ?> properties) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ServiceRegistration<?> registerService(String className, Object service, Dictionary<String, ?> properties) {
        return new ServiceRegistrationAdaptor(moduleContext.registerService(className, service, properties));
    }

    @Override
    public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
        return new ServiceRegistrationAdaptor<S>(moduleContext.registerService(clazz, service, properties));
    }

    @Override
    public ServiceReference<?>[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServiceReference<?>[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServiceReference<?> getServiceReference(String clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter) throws InvalidSyntaxException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S> S getService(ServiceReference<S> reference) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean ungetService(ServiceReference<?> reference) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getDataFile(String filename) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Filter createFilter(String filter) throws InvalidSyntaxException {
        return FrameworkUtil.createFilter(filter);
    }

    @Override
    public Bundle getBundle(String location) {
        throw new UnsupportedOperationException();
    }

    private class ServiceListenerAdaptor implements org.jboss.gravia.runtime.ServiceListener {

        private final ServiceListener delegate;

        ServiceListenerAdaptor(ServiceListener delegate) {
            this.delegate = delegate;
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public void serviceChanged(org.jboss.gravia.runtime.ServiceEvent event) {
            ServiceReference<?> sref = new ServiceReferenceAdaptor(event.getServiceReference());
            delegate.serviceChanged(new ServiceEvent(event.getType(), sref));
        }
    }

    private class ServiceReferenceAdaptor<S> implements ServiceReference<S> {

        private final org.jboss.gravia.runtime.ServiceReference<S> delegate;

        ServiceReferenceAdaptor(org.jboss.gravia.runtime.ServiceReference<S> delegate) {
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
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isAssignableTo(Bundle bundle, String className) {
            Runtime runtime = moduleContext.getModule().adapt(Runtime.class);
            Module module = runtime.getModule(bundle.getBundleId());
            return delegate.isAssignableTo(module, className);
        }

        @Override
        public int compareTo(Object reference) {
            throw new UnsupportedOperationException();
        }
    }

    private class ServiceRegistrationAdaptor<S> implements ServiceRegistration<S> {

        private final org.jboss.gravia.runtime.ServiceRegistration<S> delegate;

        ServiceRegistrationAdaptor(org.jboss.gravia.runtime.ServiceRegistration<S> delegate) {
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
}
