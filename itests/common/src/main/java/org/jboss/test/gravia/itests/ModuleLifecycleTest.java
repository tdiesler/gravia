/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.test.gravia.itests;

import java.util.concurrent.TimeUnit;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.gravia.itests.sub.ApplicationActivator;
import org.jboss.test.gravia.itests.sub.SimpleServlet;
import org.jboss.test.gravia.itests.support.HttpRequest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test webapp deployemnts
 *
 * @author thomas.diesler@jboss.com
 * @since 01-Oct-2013
 */
public abstract class ModuleLifecycleTest {

    public static WebArchive deployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "simple.war");
        archive.addClasses(HttpRequest.class, ApplicationActivator.class, SimpleServlet.class);
        archive.addClasses(ModuleLifecycleTest.class);
        return archive;
    }

    @Test
    public void testServletAccess() throws Exception {
        String result = performCall("/simple/servlet?input=Hello");
        Assert.assertEquals("Hello from Module[simple.war:1.0.0]", result);
    }

    @Test
    public void testModuleLifecycle() throws Exception {

        Module modA = RuntimeLocator.getRuntime().getModule(getClass().getClassLoader());
        Assert.assertEquals(Module.State.ACTIVE, modA.getState());

        ModuleContext context = modA.getModuleContext();
        ServiceRegistration<String> sreg = context.registerService(String.class, new String("Hello"), null);
        Assert.assertNotNull("Null sreg", sreg);

        String service = context.getService(sreg.getReference());
        Assert.assertEquals("Hello", service);

        modA.stop();
        Assert.assertEquals(Module.State.RESOLVED, modA.getState());

        modA.uninstall();
        Assert.assertEquals(Module.State.UNINSTALLED, modA.getState());

        try {
            modA.start();
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    protected String performCall(String path) throws Exception {
        String urlspec = "http://localhost:8080" + path;
        return HttpRequest.get(urlspec, 10, TimeUnit.SECONDS);
    }
}
