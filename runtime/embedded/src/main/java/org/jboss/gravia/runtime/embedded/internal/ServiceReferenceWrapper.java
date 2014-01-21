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
