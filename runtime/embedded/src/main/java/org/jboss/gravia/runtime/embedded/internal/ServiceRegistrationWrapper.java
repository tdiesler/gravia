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

import java.util.Dictionary;

import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.ServiceRegistration;


/**
 * ServiceRegistrationWrapper
 *
 * @author thomas.diesler@jboss.com
 * @since 29-Jun-2010
 */
final class ServiceRegistrationWrapper<T> implements ServiceRegistration<T> {

    private ServiceState<T> delegate;

    ServiceRegistrationWrapper(ServiceState<T> serviceState) {
        assert serviceState != null : "Null serviceState";
        this.delegate = serviceState;
    }

    @Override
    public ServiceReference<T> getReference() {
        return delegate.getReference();
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
        if (obj instanceof ServiceRegistrationWrapper == false)
            return false;
        if (obj == this)
            return true;
        ServiceRegistrationWrapper<?> other = (ServiceRegistrationWrapper<?>) obj;
        return delegate.equals(other.delegate);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
