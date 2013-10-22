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
package org.jboss.test.gravia.runtime.embedded;

import java.util.jar.Manifest;

import junit.framework.Assert;

import org.jboss.gravia.resource.spi.ManifestBuilder;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.gravia.runtime.util.ManifestHeadersProvider;
import org.jboss.test.gravia.runtime.embedded.sub.a.SimpleActivator;
import org.junit.Test;

/**
 * Test basic runtime functionality.
 *
 * @author thomas.diesler@jbos.com
 * @since 27-Jan-2012
 */
public class ModuleLifecycleTestCase extends AbstractRuntimeTest {

    @Test
    public void testBasicModule() throws Exception {

        Manifest manifest = new ManifestBuilder().addIdentityCapability("moduleA", "1.0.0").getManifest();
        ManifestHeadersProvider headersProvider = new ManifestHeadersProvider(manifest);

        Module modA = getRuntime().installModule(SimpleActivator.class.getClassLoader(), headersProvider.getHeaders());
        Assert.assertEquals(Module.State.RESOLVED, modA.getState());

        modA.start();
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

    @Test
    public void testModuleActivator() throws Exception {

        ManifestBuilder builder = new ManifestBuilder().addIdentityCapability("moduleA", "1.0.0");
        builder.addModuleActivator(SimpleActivator.class);
        ManifestHeadersProvider headersProvider = new ManifestHeadersProvider(builder.getManifest());

        Module modA = getRuntime().installModule(SimpleActivator.class.getClassLoader(), headersProvider.getHeaders());
        Assert.assertEquals(Module.State.RESOLVED, modA.getState());

        ModuleContext ctxA = modA.getModuleContext();
        Assert.assertNull("Null moduleContext", ctxA);

        modA.start();
        Assert.assertEquals(Module.State.ACTIVE, modA.getState());

        ctxA = modA.getModuleContext();
        ServiceReference<String> srefA = ctxA.getServiceReference(String.class);
        Assert.assertNotNull("Null sref", srefA);

        String srvA = ctxA.getService(srefA);
        Assert.assertEquals("Hello", srvA);

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
}
