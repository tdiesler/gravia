/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.gravia.runtime.embedded.sub.a;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.test.gravia.runtime.embedded.sub.a1.ServiceD1;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(service = { ServiceD.class }, immediate = true)
public class ServiceD {

    static AtomicInteger INSTANCE_COUNT = new AtomicInteger();
    final String name = getClass().getSimpleName() + "#" + INSTANCE_COUNT.incrementAndGet();

    final AtomicReference<ServiceD1> ref = new AtomicReference<ServiceD1>();
    final CountDownLatch activateLatch = new CountDownLatch(1);
    final CountDownLatch deactivateLatch = new CountDownLatch(1);

    @Activate
    void activate(ComponentContext context) {
        activateLatch.countDown();
    }

    @Deactivate
    void deactivate() {
        deactivateLatch.countDown();
    }

    public boolean awaitActivate(long timeout, TimeUnit unit) throws InterruptedException {
        return activateLatch.await(timeout, unit);
    }

    public boolean awaitDeactivate(long timeout, TimeUnit unit) throws InterruptedException {
        return deactivateLatch.await(timeout, unit);
    }

    @Reference
    void bindServiceD1(ServiceD1 service) {
        ref.set(service);
    }

    void unbindServiceD1(ServiceD1 service) {
        ref.compareAndSet(service, null);
    }

    public ServiceD1 getServiceD1() {
        return ref.get();
    }

    public String doStuff(String msg) {
        ServiceD1 srv = ref.get();
        return name + ":" + srv.doStuff(msg);
    }

    @Override
    public String toString() {
        return name;
    }
}