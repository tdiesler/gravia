/*
 * #%L
 * Gravia :: Integration Tests :: Common
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
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
package org.jboss.test.gravia.itests;

import java.io.InputStream;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.osgi.StartLevelAware;
import org.jboss.gravia.Constants;
import org.jboss.gravia.itests.support.ArchiveBuilder;
import org.jboss.gravia.runtime.BundleActivatorBridge;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleActivator;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceLocator;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test simple module lifecycle
 *
 * @author thomas.diesler@jboss.com
 * @since 01-Oct-2013
 */
@RunWith(Arquillian.class)
public class ModuleLifecycleTest {

    @Deployment
    @StartLevelAware(autostart = true)
    public static Archive<?> deployment() {
        final ArchiveBuilder archive = new ArchiveBuilder("simple-module");
        archive.addClasses(ModuleLifecycleTest.class, Activator.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleVersion("1.0.0");
                builder.addBundleActivator(BundleActivatorBridge.class);
                builder.addManifestHeader(Constants.MODULE_ACTIVATOR, Activator.class.getName());
                builder.addImportPackages(RuntimeLocator.class);
                return builder.openStream();
            }
        });
        return archive.getArchive();
    }

    @Test
    public void testModuleLifecycle() throws Exception {

        // Verify that the Runtime service is registered
        Runtime runtime = ServiceLocator.getRequiredService(Runtime.class);

        Module modA = runtime.getModule(getClass().getClassLoader());
        Assert.assertEquals(Module.State.ACTIVE, modA.getState());

        String symbolicName = modA.getHeaders().get("Bundle-SymbolicName");
        Assert.assertEquals("simple-module", symbolicName);

        ModuleContext context = modA.getModuleContext();
        Module sysmodule = runtime.getModule(0);
        symbolicName = sysmodule.getHeaders().get("Bundle-SymbolicName");
        Assert.assertNotNull("System bundle symbolic name not null", symbolicName);

        ServiceReference<String> sref = context.getServiceReference(String.class);
        String service = context.getService(sref);
        Assert.assertEquals("Hello", service);

        modA.stop();
        Assert.assertEquals(Module.State.INSTALLED, modA.getState());

        modA.uninstall();
        Assert.assertEquals(Module.State.UNINSTALLED, modA.getState());

        try {
            modA.start();
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public static class Activator implements ModuleActivator {

        ServiceRegistration<String> sreg;

        @Override
        public void start(ModuleContext context) throws Exception {
            sreg = context.registerService(String.class, new String("Hello"), null);
        }

        @Override
        public void stop(ModuleContext context) throws Exception {
            sreg.unregister();
        }
    }
}
