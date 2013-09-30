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
package org.jboss.test.gravia.runtime.embedded.sub.a1;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

@Component(service = { ServiceD1.class })
public class ServiceD1 {

    static AtomicInteger INSTANCE_COUNT = new AtomicInteger();
    final String name = getClass().getSimpleName() + "#" + INSTANCE_COUNT.incrementAndGet();

    final CountDownLatch modifiedLatch = new CountDownLatch(1);
    private volatile Map<String, String> config;

    @Activate
    void activate(ComponentContext context, Map<String, String> config) {
        this.config = config;
    }

    @Modified
    void modified(Map<String, String> config) {
        this.config = config;
        modifiedLatch.countDown();
    }

    @Deactivate
    void deactivate() {
    }

    public boolean awaitModified(long timeout, TimeUnit unit) throws InterruptedException {
        return modifiedLatch.await(timeout, unit);
    }

    public String doStuff(String msg) {
        String fooval = config.get("foo");
        return name + ":" + fooval + ":" + msg;
    }

    @Override
    public String toString() {
        return name;
    }
}