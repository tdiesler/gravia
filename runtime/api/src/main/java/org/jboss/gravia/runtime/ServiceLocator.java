/*
 * #%L
 * Fabric8 :: API
 * %%
 * Copyright (C) 2014 Red Hat
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

package org.jboss.gravia.runtime;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.gravia.utils.IllegalStateAssertion;

/**
 * Locate a service in the {@link Runtime}
 *
 * @author thomas.diesler@jboss.com
 * @since 14-Mar-2014
 */
public final class ServiceLocator {

	public static final Long DEFAULT_TIMEOUT = 10000L;

	private ServiceLocator() {
		//Utility Class
	}

    public static <T> T getService(Class<T> type) {
        ModuleContext moduleContext = RuntimeLocator.getRequiredRuntime().getModuleContext();
        ServiceReference<T> sref = moduleContext.getServiceReference(type);
        return sref != null ? moduleContext.getService(sref) : null;
    }

    public static <T> T getService(ModuleContext moduleContext, Class<T> type) {
        ServiceReference<T> sref = moduleContext.getServiceReference(type);
        return sref != null ? moduleContext.getService(sref) : null;
    }

    public static <T> T getRequiredService(Class<T> type) {
        return getRequiredService(RuntimeLocator.getRequiredRuntime().getModuleContext(), type);
    }

    public static <T> T getRequiredService(ModuleContext moduleContext, Class<T> type) {
        T service = getService(moduleContext, type);
        IllegalStateAssertion.assertNotNull(service, "Service not available: " + type.getName());
        return service;
    }

    public static <T> T awaitService(Class<T> type) {
        ModuleContext moduleContext = RuntimeLocator.getRequiredRuntime().getModuleContext();
        return awaitService(moduleContext, type, null, DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public static <T> T awaitService(ModuleContext moduleContext, Class<T> type) {
        return awaitService(moduleContext, type, null, DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public static <T> T awaitService(Class<T> type, String filterspec) {
        ModuleContext moduleContext = RuntimeLocator.getRequiredRuntime().getModuleContext();
        return awaitService(moduleContext, type, filterspec, DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public static <T> T awaitService(Class<T> type, long timeout, TimeUnit unit) {
        ModuleContext moduleContext = RuntimeLocator.getRequiredRuntime().getModuleContext();
        return awaitService(moduleContext, type, null, timeout, unit);
    }

    public static <T> T awaitService(Class<T> type, String filterspec, long timeout, TimeUnit unit) {
        ModuleContext moduleContext = RuntimeLocator.getRequiredRuntime().getModuleContext();
        return awaitService(moduleContext, type, filterspec, timeout, unit);
    }

    public static <T> T awaitService(final ModuleContext moduleContext, Class<T> type, String filterspec, long timeout, TimeUnit unit) {

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<T> serviceRef = new AtomicReference<T>();
        final Filter serviceFilter = filterspec != null ? moduleContext.createFilter(filterspec) : null;
        ServiceTracker<T, T> tracker = new ServiceTracker<T, T>(moduleContext, type, null) {
            @Override
            public T addingService(ServiceReference<T> sref) {
                T service = super.addingService(sref);
                if (serviceFilter == null || serviceFilter.match(sref)) {
                    serviceRef.set(moduleContext.getService(sref));
                    latch.countDown();
                }
                return service;
            }
        };
        tracker.open();
        try {
            if (!latch.await(timeout, unit)) {
                String srvspec = (type != null ? type.getName() : "") + (serviceFilter != null ? serviceFilter : "");
                throw new IllegalStateException("Cannot obtain service: " + srvspec);
            }
            return serviceRef.get();
        } catch (InterruptedException ex) {
            throw new IllegalStateException();
        } finally {
            tracker.close();
        }
	}
}
