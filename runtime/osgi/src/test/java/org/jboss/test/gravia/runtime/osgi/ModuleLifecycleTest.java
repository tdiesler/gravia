/*
 * #%L
 * Gravia :: Runtime :: OSGi
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
package org.jboss.test.gravia.runtime.osgi;

import java.io.InputStream;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.osgi.StartLevelAware;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.gravia.Constants;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.Module.State;
import org.jboss.gravia.runtime.BundleActivatorBridge;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.osgi.spi.OSGiRuntimeLocator;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.gravia.runtime.osgi.sub.a.SimpleModuleActivator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Test simple module lifecycle
 *
 * @author thomas.diesler@jboss.com
 * @since 01-Oct-2013
 */
@RunWith(Arquillian.class)
public class ModuleLifecycleTest {

    @ArquillianResource
    BundleContext bundleContext;

    @Before
    public void setUp() throws ModuleException {
        Runtime runtime = OSGiRuntimeLocator.createRuntime(bundleContext);
        runtime.init();
    }

    @After
    public void tearDown() {
        OSGiRuntimeLocator.releaseRuntime();
    }

    @Deployment
    @StartLevelAware(autostart = true)
    public static JavaArchive deployment() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "example-bundle");
        archive.addClasses(BundleActivatorBridge.class, SimpleModuleActivator.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addBundleActivator(BundleActivatorBridge.class);
                builder.addImportPackages(OSGiRuntimeLocator.class, Module.class, OSGiRuntimeLocator.class, Resource.class);
                builder.addManifestHeader(Constants.MODULE_ACTIVATOR, SimpleModuleActivator.class.getName());
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testSystemModule() throws Exception {
        Module module = RuntimeLocator.getRequiredRuntime().getModule(0);
        Assert.assertEquals("gravia-system:0.0.0", module.getIdentity().toString());
        Assert.assertEquals(State.ACTIVE, module.getState());
    }

    @Test
    public void testModuleLifecycle(@ArquillianResource Bundle bundle) throws Exception {

        Module module = RuntimeLocator.getRequiredRuntime().getModule(bundle.getBundleId());
        Assert.assertEquals(bundle.getBundleId(), module.getModuleId());

        Assert.assertEquals("example-bundle:0.0.0", module.getIdentity().toString());

        module.start();
        Assert.assertEquals(State.ACTIVE, module.getState());

        module.stop();
        Assert.assertEquals(State.INSTALLED, module.getState());
        Assert.assertNull("ModuleContext null", module.getModuleContext());

        module.start();
        Assert.assertEquals(State.ACTIVE, module.getState());

        ModuleContext context = module.getModuleContext();
        ServiceReference<String> sref = context.getServiceReference(String.class);
        Assert.assertNotNull("ServiceReference not null", sref);

        Assert.assertEquals("Hello", context.getService(sref));
    }
}
