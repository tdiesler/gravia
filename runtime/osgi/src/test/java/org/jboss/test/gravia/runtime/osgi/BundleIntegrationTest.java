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
package org.jboss.test.gravia.runtime.osgi;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.ConstantsHelper;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleEvent;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.ModuleListener;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.SynchronousModuleListener;
import org.jboss.gravia.runtime.osgi.spi.OSGiRuntimeLocator;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.gravia.runtime.osgi.sub.a.SimpleBundleActivator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

/**
 * Test a plain bundle as {@link Module}
 *
 * @author thomas.diesler@jboss.com
 * @since 23-Jan-2014
 */
@RunWith(Arquillian.class)
public class BundleIntegrationTest {

    private static final String BUNDLE_B = "bundleB";

    @ArquillianResource
    Deployer deployer;

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
    public static Archive<?> deployment() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "simple-bundle-test");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleVersion("1.0.0");
                builder.addImportPackages(OSGiRuntimeLocator.class, Runtime.class, Resource.class);
                builder.addImportPackages(BundleWiring.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testModuleLifecycle() throws Exception {
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        ModuleContext rtcontext = runtime.getModuleContext();

        ServiceReference<String> sref = rtcontext.getServiceReference(String.class);
        Assert.assertNull("ServiceReference null", sref);

        final List<String> events = new ArrayList<String>();
        ModuleListener listener = new SynchronousModuleListener() {
            @Override
            public void moduleChanged(ModuleEvent event) {
                Module module = event.getModule();
                String modid = module.getIdentity().toString();
                String evtid = ConstantsHelper.moduleEvent(event.getType());
                events.add(modid + ":" + evtid);
            }
        };
        rtcontext.addModuleListener(listener);

        // Install the Bundle
        InputStream input = deployer.getDeployment(BUNDLE_B);
        Bundle bundleB = bundleContext.installBundle(BUNDLE_B, input);
        Assert.assertTrue("No module events", events.isEmpty());
        Module moduleB = runtime.getModule(bundleB.getBundleId());
        Assert.assertNull("Module null", moduleB);

        // Resolve the Bundle
        URL resURL = bundleB.getResource(JarFile.MANIFEST_NAME);
        Assert.assertNotNull("Manifest not null", resURL);
        Assert.assertEquals("Bundle RESOLVED", Bundle.RESOLVED, bundleB.getState());

        // Verify that the Module is available
        moduleB = runtime.getModule(bundleB.getBundleId());
        Assert.assertNotNull("Module not null", moduleB);
        Assert.assertEquals(Module.State.INSTALLED, moduleB.getState());
        ModuleContext contextB = moduleB.getModuleContext();
        Assert.assertNull("ModuleContext null", contextB);

        // Verify Module events
        Assert.assertEquals("Module events", 1, events.size());
        Assert.assertEquals("bundleB:1.0.0:INSTALLED", events.get(0));

        // Start the Bundle
        bundleB.start();
        Assert.assertEquals("Bundle ACTIVE", Bundle.ACTIVE, bundleB.getState());
        Assert.assertEquals(Module.State.ACTIVE, moduleB.getState());
        contextB = moduleB.getModuleContext();
        Assert.assertNotNull("ModuleContext not null", contextB);
        sref = contextB.getServiceReference(String.class);
        Assert.assertNotNull("ServiceReference not null", sref);
        Assert.assertEquals("bundleB:1.0.0", contextB.getService(sref));

        // Verify Module events
        Assert.assertEquals("Module events", 3, events.size());
        Assert.assertEquals("bundleB:1.0.0:STARTING", events.get(1));
        Assert.assertEquals("bundleB:1.0.0:STARTED", events.get(2));

        // Stop the Bundle
        bundleB.stop();
        Assert.assertEquals("Bundle RESOLVED", Bundle.RESOLVED, bundleB.getState());
        Assert.assertEquals(Module.State.INSTALLED, moduleB.getState());
        contextB = moduleB.getModuleContext();
        Assert.assertNull("ModuleContext null", contextB);
        sref = rtcontext.getServiceReference(String.class);
        Assert.assertNull("ServiceReference null", sref);

        // Verify Module events
        Assert.assertEquals("Module events", 5, events.size());
        Assert.assertEquals("bundleB:1.0.0:STOPPING", events.get(3));
        Assert.assertEquals("bundleB:1.0.0:STOPPED", events.get(4));

        // Uninstall the Bundle
        bundleB.uninstall();
        Assert.assertEquals("Bundle UNINSTALLED", Bundle.UNINSTALLED, bundleB.getState());
        Assert.assertEquals(Module.State.UNINSTALLED, moduleB.getState());

        // Verify Module events
        Assert.assertEquals("Module events", 6, events.size());
        Assert.assertEquals("bundleB:1.0.0:UNINSTALLED", events.get(5));
    }

    @Deployment(name = BUNDLE_B, testable = false, managed = false)
    public static Archive<?> bundleB() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, BUNDLE_B);
        archive.addClasses(SimpleBundleActivator.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleVersion("1.0.0");
                builder.addImportPackages(BundleActivator.class);
                builder.addBundleActivator(SimpleBundleActivator.class);
                return builder.openStream();
            }
        });
        return archive;
    }
}
