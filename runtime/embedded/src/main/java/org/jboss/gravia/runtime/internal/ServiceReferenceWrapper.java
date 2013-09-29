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
package org.jboss.gravia.runtime.internal;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ServiceReference;


/**
 * ServiceReferenceWrapper
 *
 * @author thomas.diesler@jboss.com
 * @since 29-Jun-2010
 */
final class ServiceReferenceWrapper<T> implements ServiceReference<T> {

    private ServiceState<T> delegate;

    ServiceReferenceWrapper(ServiceState<T> serviceState) {
        assert serviceState != null : "Null serviceState";
        this.delegate = serviceState;
    }

    ServiceState<T> getServiceState() {
        return delegate;
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
        return delegate.getModule();
    }

    @Override
    public boolean isAssignableTo(Module bundle, String className) {
        return delegate.isAssignableTo(bundle, className);
    }

    @Override
    public int compareTo(Object reference) {
        return delegate.compareTo(reference);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceReferenceWrapper == false)
            return false;
        if (obj == this)
            return true;
        ServiceReferenceWrapper<?> other = (ServiceReferenceWrapper<?>) obj;
        return delegate.equals(other.delegate);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
