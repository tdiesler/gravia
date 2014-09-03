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
import org.jboss.test.gravia.runtime.osgi.sub.a.AttachedType;
import org.jboss.test.gravia.runtime.osgi.sub.a.OSGiTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;

/**
 * Test fragment deployment;
 *
 * @author Thomas.Diesler@jboss.com
 */
@RunWith(Arquillian.class)
public class FragmentTest {

    static final String GOOD_BUNDLE = "good-bundle";
    static final String GOOD_FRAGMENT = "good-fragment";

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
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "fragment-test");
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
    public void testAttachedFragment() throws Exception {

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

        // Deploy the fragment
        InputStream input = deployer.getDeployment(GOOD_FRAGMENT);
        Bundle fragment = bundleContext.installBundle(GOOD_FRAGMENT, input);
        try {
            Assert.assertTrue(events.isEmpty());
            
            // Deploy the bundle
            input = deployer.getDeployment(GOOD_BUNDLE);
            Bundle host = bundleContext.installBundle("bundle-host-attached", input);
            try {
                Class<?> clazz = OSGiTestHelper.assertLoadClass(host, "org.jboss.test.gravia.runtime.osgi.sub.a.AttachedType");
                Assert.assertSame(host, ((BundleReference) clazz.getClassLoader()).getBundle());
                Assert.assertEquals(1, events.size());
                Assert.assertEquals("good-bundle:0.0.0:INSTALLED", events.remove(0));
            } finally {
                host.uninstall();
                Assert.assertEquals(1, events.size());
                Assert.assertEquals("good-bundle:0.0.0:UNINSTALLED", events.remove(0));
            }
        } finally {
            fragment.uninstall();
            Assert.assertTrue(events.isEmpty());
        }
    }

    @Test
    public void testUnattachedFragment() throws Exception {

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

        // Deploy the bundle
        InputStream input = deployer.getDeployment(GOOD_BUNDLE);
        Bundle host = bundleContext.installBundle("bundle-host", input);
        try {
            Assert.assertTrue(events.isEmpty());
            OSGiTestHelper.assertLoadClassFail(host, "org.jboss.test.gravia.runtime.osgi.sub.a.AttachedType");
            Assert.assertEquals(1, events.size());
            Assert.assertEquals("good-bundle:0.0.0:INSTALLED", events.remove(0));
            
            // Deploy the fragment
            input = deployer.getDeployment(GOOD_FRAGMENT);
            Bundle fragment = bundleContext.installBundle(GOOD_FRAGMENT, input);
            try {
                OSGiTestHelper.assertLoadClassFail(host, "org.jboss.test.gravia.runtime.osgi.sub.a.AttachedType");
                Assert.assertTrue(events.isEmpty());
            } finally {
                fragment.uninstall();
                Assert.assertTrue(events.isEmpty());
            }
        } finally {
            host.uninstall();
            Assert.assertEquals(1, events.size());
            Assert.assertEquals("good-bundle:0.0.0:UNINSTALLED", events.remove(0));
        }
    }

    @Deployment(name = GOOD_BUNDLE, managed = false, testable = false)
    public static JavaArchive getGoodBundleArchive() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, GOOD_BUNDLE);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                return builder.openStream();
            }
        });
        return archive;
    }

    @Deployment(name = GOOD_FRAGMENT, managed = false, testable = false)
    public static JavaArchive getGoodFragmentArchive() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, GOOD_FRAGMENT);
        archive.addClasses(AttachedType.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addFragmentHost(GOOD_BUNDLE);
                return builder.openStream();
            }
        });
        return archive;
    }
}