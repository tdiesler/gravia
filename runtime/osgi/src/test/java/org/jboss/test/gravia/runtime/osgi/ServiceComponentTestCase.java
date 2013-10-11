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
package org.jboss.test.gravia.runtime.osgi;

import java.io.InputStream;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.gravia.resource.Constants;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleActivator;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.osgi.DefaultActivator;
import org.jboss.gravia.runtime.osgi.OSGiRuntimeLocator;
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
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

/**
 * Test basic SCR Component
 *
 * @author thomas.diesler@jbos.com
 * @since 04-Oct-2013
 */
@RunWith(Arquillian.class)
public class ServiceComponentTestCase  {

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
                builder.addImportPackages(Module.class);
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
        archive.addClasses(ServiceA.class, DefaultActivator.class);
        archive.addAsResource("OSGI-INF/org.jboss.test.gravia.runtime.osgi.sub.a.ServiceA.xml");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleVersion("1.0.0");
                builder.addBundleActivator(DefaultActivator.class);
                builder.addExportPackages(ServiceA.class);
                builder.addImportPackages(BundleActivator.class, ModuleActivator.class, OSGiRuntimeLocator.class, ComponentContext.class);
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
        archive.addClasses(ServiceA1.class, DefaultActivator.class);
        archive.addAsResource("OSGI-INF/org.jboss.test.gravia.runtime.osgi.sub.a1.ServiceA1.xml");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleVersion("1.0.0");
                builder.addBundleActivator(DefaultActivator.class);
                builder.addImportPackages(BundleActivator.class, ModuleActivator.class, OSGiRuntimeLocator.class, ComponentContext.class);
                builder.addExportPackages(ServiceA1.class);
                builder.addManifestHeader(Constants.GRAVIA_IDENTITY_CAPABILITY, BUNDLE_A1 + ";version=1.0.0");
                builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.runtime.osgi.sub.a1.ServiceA1.xml");
                return builder.openStream();
            }
        });
        return archive;
    }
}
