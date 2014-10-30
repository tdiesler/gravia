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

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.gravia.Constants;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.osgi.spi.OSGiRuntimeLocator;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.gravia.runtime.osgi.sub.a.ServiceA;
import org.jboss.test.gravia.runtime.osgi.sub.a1.ServiceA1;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

/**
 * Test basic SCR Component
 *
 * @author thomas.diesler@jboss.com
 * @since 04-Oct-2013
 */
@RunWith(Arquillian.class)
public class ServiceComponentTest  {

    static final String BUNDLE_A = "bundleA";
    static final String BUNDLE_A1 = "bundleA1";

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
    public static JavaArchive createdeployment() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "service-components-test");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(OSGiRuntimeLocator.class, Module.class, ComponentContext.class);
                builder.addDynamicImportPackages(ServiceA.class, OSGiRuntimeLocator.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testBasicModule() throws Exception {

        Bundle bundleA = bundleContext.installBundle(BUNDLE_A, deployer.getDeployment(BUNDLE_A));
        Bundle bundleA1 = bundleContext.installBundle(BUNDLE_A1, deployer.getDeployment(BUNDLE_A1));

        bundleA.start();
        bundleA1.start();

        Module modA = OSGiRuntimeLocator.getRuntime().getModule(bundleA.getBundleId());
        ModuleContext ctxA = modA.getModuleContext();
        ServiceReference<ServiceA> srefA = ctxA.getServiceReference(ServiceA.class);
        Assert.assertNotNull("ServiceReference not null", srefA);

        ServiceA srvA = ctxA.getService(srefA);
        Assert.assertEquals("ServiceA#1:ServiceA1#1:Hello", srvA.doStuff("Hello"));
    }

    @Deployment(name = BUNDLE_A, managed = false, testable = false)
    public static JavaArchive getBundleA() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, BUNDLE_A);
        archive.addClasses(ServiceA.class);
        archive.addAsResource("OSGI-INF/org.jboss.test.gravia.runtime.osgi.sub.a.ServiceA.xml");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleVersion("1.0.0");
                builder.addExportPackages(ServiceA.class);
                builder.addImportPackages(OSGiRuntimeLocator.class, ComponentContext.class);
                builder.addImportPackages(ServiceA1.class);
                builder.addManifestHeader(Constants.GRAVIA_IDENTITY_CAPABILITY, BUNDLE_A + ";version=1.0.0");
                builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.runtime.osgi.sub.a.ServiceA.xml");
                return builder.openStream();
            }
        });
        return archive;
    }

    @Deployment(name = BUNDLE_A1, managed = false, testable = false)
    public static JavaArchive getBundleA1() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, BUNDLE_A1);
        archive.addClasses(ServiceA1.class);
        archive.addAsResource("OSGI-INF/org.jboss.test.gravia.runtime.osgi.sub.a1.ServiceA1.xml");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleVersion("1.0.0");
                builder.addImportPackages(OSGiRuntimeLocator.class, ComponentContext.class);
                builder.addExportPackages(ServiceA1.class);
                builder.addManifestHeader(Constants.GRAVIA_IDENTITY_CAPABILITY, BUNDLE_A1 + ";version=1.0.0");
                builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.runtime.osgi.sub.a1.ServiceA1.xml");
                return builder.openStream();
            }
        });
        return archive;
    }
}
