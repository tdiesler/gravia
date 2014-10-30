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
import java.util.ArrayList;
import java.util.List;

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
import org.jboss.gravia.runtime.SynchronousModuleListener;
import org.jboss.gravia.runtime.osgi.spi.OSGiRuntimeLocator;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.gravia.runtime.osgi.sub.a.OSGiTestHelper;
import org.jboss.test.gravia.runtime.osgi.sub.b1.B1;
import org.jboss.test.gravia.runtime.osgi.sub.b2.B2;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Test Bundle.update();
 *
 * @author Thomas.Diesler@jboss.com
 */
@RunWith(Arquillian.class)
public class BundleUpdateTest {

    private static final String BUNDLE_REV0 = "bundle-rev0";
    private static final String BUNDLE_REV1 = "bundle-rev1";

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
    public static JavaArchive create() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "bundle-update-test");
        archive.addClasses(OSGiTestHelper.class);
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(OSGiRuntimeLocator.class, Runtime.class, Resource.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testUpdateBundle() throws Exception {
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        ModuleContext rtcontext = runtime.getModuleContext();

        final List<String> events = new ArrayList<String>();
        ModuleListener listener = new SynchronousModuleListener() {
            @Override
            public void moduleChanged(ModuleEvent event) {
                Module module = event.getModule();
                String modid = module.getIdentity().toString();
                String evtid = ConstantsHelper.moduleEvent(event.getType());
                String message = modid + ":" + evtid;
                events.add(message);
            }
        };
        rtcontext.addModuleListener(listener);

        InputStream input = deployer.getDeployment(BUNDLE_REV0);
        Bundle bundle = bundleContext.installBundle(BUNDLE_REV0, input);
        try {
            Assert.assertTrue(events.isEmpty());

            bundle.start();
            OSGiTestHelper.assertLoadClass(bundle, "org.jboss.test.gravia.runtime.osgi.sub.b1.B1");
            OSGiTestHelper.assertLoadClassFail(bundle, "org.jboss.test.gravia.runtime.osgi.sub.b2.B2");

            Assert.assertEquals(3, events.size());
            Assert.assertEquals("update-test:1.0.0:INSTALLED", events.remove(0));
            Assert.assertEquals("update-test:1.0.0:STARTING", events.remove(0));
            Assert.assertEquals("update-test:1.0.0:STARTED", events.remove(0));

            InputStream is = deployer.getDeployment(BUNDLE_REV1);
            bundle.update(is);
            OSGiTestHelper.assertLoadClass(bundle, "org.jboss.test.gravia.runtime.osgi.sub.b2.B2");
            OSGiTestHelper.assertLoadClassFail(bundle, "org.jboss.test.gravia.runtime.osgi.sub.b1.B1");

            Assert.assertEquals(6, events.size());
            Assert.assertEquals("update-test:1.0.0:STOPPING", events.remove(0));
            Assert.assertEquals("update-test:1.0.0:STOPPED", events.remove(0));
            Assert.assertEquals("update-test:1.0.0:UNINSTALLED", events.remove(0));
            Assert.assertEquals("update-test:2.0.0:INSTALLED", events.remove(0));
            Assert.assertEquals("update-test:2.0.0:STARTING", events.remove(0));
            Assert.assertEquals("update-test:2.0.0:STARTED", events.remove(0));
        } finally {
            bundle.uninstall();
        }

        Assert.assertEquals(3, events.size());
        Assert.assertEquals("update-test:2.0.0:STOPPING", events.remove(0));
        Assert.assertEquals("update-test:2.0.0:STOPPED", events.remove(0));
        Assert.assertEquals("update-test:2.0.0:UNINSTALLED", events.remove(0));
    }

    @Deployment(name = BUNDLE_REV0, managed = false, testable = false)
    public static JavaArchive getInitialBundle() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, BUNDLE_REV0);
        archive.addClass(B1.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName("update-test");
                builder.addBundleVersion("1.0.0");
                builder.addExportPackages(B1.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Deployment(name = BUNDLE_REV1, managed = false, testable = false)
    public static JavaArchive getUpdatedBundle() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, BUNDLE_REV1);
        archive.addClass(B2.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName("update-test");
                builder.addBundleVersion("2.0.0");
                builder.addExportPackages(B2.class);
                return builder.openStream();
            }
        });
        return archive;
    }
}