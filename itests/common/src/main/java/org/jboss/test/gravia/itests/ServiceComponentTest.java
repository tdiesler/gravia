/*
 * #%L
 * JBossOSGi SPI
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
package org.jboss.test.gravia.itests;

import java.util.concurrent.TimeUnit;

import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.test.gravia.itests.sub.a.ServiceA;
import org.jboss.test.gravia.itests.support.HttpRequest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test basic SCR Component
 *
 * @author thomas.diesler@jbos.com
 * @since 04-Oct-2013
 */
public abstract class ServiceComponentTest  {

    @Test
    public void testBasicModule() throws Exception {

        String result = performCall("/scr-test/servlet?input=Hello");
        Assert.assertEquals("Hello from Module[scr-test:1.0.0]", result);

        ResourceIdentity identity = ResourceIdentity.fromString("scr-test:1.0.0");
        Runtime runtime = RuntimeLocator.getRuntime();
        Module modA = runtime.getModule(identity);

        ModuleContext ctxA = modA.getModuleContext();
        ServiceReference<ServiceA> srefA = ctxA.getServiceReference(ServiceA.class);
        Assert.assertNotNull("ServiceReference not null", srefA);

        ServiceA srvA = ctxA.getService(srefA);
        Assert.assertEquals("ServiceA#1:ServiceA1#1:Hello", srvA.doStuff("Hello"));
    }

    protected String performCall(String path) throws Exception {
        String urlspec = "http://localhost:8080" + path;
        return HttpRequest.get(urlspec, 10, TimeUnit.SECONDS);
    }
}
