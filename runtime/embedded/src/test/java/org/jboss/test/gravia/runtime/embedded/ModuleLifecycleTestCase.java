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
package org.jboss.test.gravia.runtime.embedded;

import java.util.jar.Manifest;

import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.gravia.runtime.util.ManifestHeadersProvider;
import org.jboss.test.gravia.runtime.embedded.sub.a.SimpleModuleActivator;
import org.jboss.test.gravia.runtime.embedded.support.AbstractEmbeddedRuntimeTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test basic runtime functionality.
 *
 * @author thomas.diesler@jbos.com
 * @since 27-Jan-2012
 */
public class ModuleLifecycleTestCase extends AbstractEmbeddedRuntimeTest {

    @Test
    public void testBasicModule() throws Exception {

        Manifest manifest = new ManifestBuilder().addIdentityCapability("moduleA", "1.0.0").getManifest();
        ManifestHeadersProvider headersProvider = new ManifestHeadersProvider(manifest);

        Module modA = getRuntime().installModule(SimpleModuleActivator.class.getClassLoader(), headersProvider.getHeaders());
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
        builder.addModuleActivator(SimpleModuleActivator.class);
        ManifestHeadersProvider headersProvider = new ManifestHeadersProvider(builder.getManifest());

        Module modA = getRuntime().installModule(SimpleModuleActivator.class.getClassLoader(), headersProvider.getHeaders());
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
