/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.test.gravia.runtime.osgi;

import java.io.InputStream;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.Module.State;
import org.jboss.gravia.runtime.ModuleActivator;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.osgi.OSGiRuntime;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.gravia.runtime.osgi.sub.SimpleActivator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Test simple module lifecycle
 * 
 * @author thomas.diesler@jboss.com
 * @since 01-Oct-2013
 */
@RunWith(Arquillian.class)
public class ModuleLifecycleTestCase {

    @ArquillianResource
    BundleContext syscontext;

    @Before
    public void setUp() throws ModuleException {
        OSGiRuntime runtime = new OSGiRuntime(syscontext);
        RuntimeLocator.setRuntime(runtime);
        runtime.init();
        runtime.start();
    }

    @After
    public void tearDown() throws ModuleException {
        Runtime runtime = RuntimeLocator.getRuntime();
        runtime.stop();
        runtime.destroy();
    }

    @Deployment
    public static JavaArchive createdeployment() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "example-bundle");
        archive.addClasses(SimpleActivator.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addBundleActivator(SimpleActivator.class);
                builder.addImportPackages(BundleActivator.class, ModuleActivator.class, OSGiRuntime.class, Resource.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testBundle(@ArquillianResource Bundle bundle) throws Exception {

        Module module = OSGiRuntime.mappedModule(bundle);
        Assert.assertEquals(bundle.getBundleId(), module.getModuleId());

        Assert.assertEquals("example-bundle:0.0.0", module.getIdentity().toString());

        module.start();
        Assert.assertEquals(State.ACTIVE, module.getState());

        module.stop();
        Assert.assertEquals(State.RESOLVED, module.getState());
        Assert.assertNull("ModuleContext null", module.getModuleContext());

        module.start();
        Assert.assertEquals(State.ACTIVE, module.getState());

        ModuleContext context = module.getModuleContext();
        ServiceReference<String> sref = context.getServiceReference(String.class);
        Assert.assertNotNull("ServiceReference not null", sref);

        Assert.assertEquals("Hello", context.getService(sref));
    }
}
