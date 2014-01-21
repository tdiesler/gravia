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
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.osgi.StartLevelAware;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.gravia.Constants;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ModuleException;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.osgi.OSGiRuntimeLocator;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.gravia.runtime.osgi.sub.d.ServiceD;
import org.jboss.test.gravia.runtime.osgi.sub.d1.ServiceD1;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;

/**
 * Test basic SCR Component
 *
 * @author thomas.diesler@jbos.com
 * @since 04-Oct-2013
 */
@RunWith(Arquillian.class)
public class ConfigurationAdminTestCase  {

    static final String BUNDLE_D = "bundleD";
    static final String BUNDLE_D1 = "bundleD1";

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
    @StartLevelAware(autostart = true)
    public static JavaArchive createdeployment() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "configuration-admin-test");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);
                builder.addImportPackages(Module.class, ConfigurationAdmin.class);
                builder.addDynamicImportPackages(ServiceD.class, ServiceD1.class, Runtime.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public void testBasicModule() throws Exception {

        Bundle bundleD = bundleContext.installBundle(BUNDLE_D, deployer.getDeployment(BUNDLE_D));
        Bundle bundleD1 = bundleContext.installBundle(BUNDLE_D1, deployer.getDeployment(BUNDLE_D1));

        bundleD.start();
        bundleD1.start();

        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Module modD = runtime.getModule(bundleD.getBundleId());
        Module modD1 = runtime.getModule(bundleD1.getBundleId());

        ModuleContext ctxD = modD.getModuleContext();
        ServiceReference<ServiceD> srefA = ctxD.getServiceReference(ServiceD.class);
        Assert.assertNotNull("ServiceReference not null", srefA);

        ServiceD srvD = ctxD.getService(srefA);
        Assert.assertEquals("ServiceD#1:ServiceD1#1:null:Hello", srvD.doStuff("Hello"));

        ConfigurationAdmin configAdmin = getConfigurationAdmin(modD1);
        Configuration config = configAdmin.getConfiguration(ServiceD1.class.getName());
        Assert.assertNotNull("Config not null", config);
        Assert.assertNull("Config is empty, but was: " + config.getProperties(), config.getProperties());

        Dictionary<String, String> configProps = new Hashtable<String, String>();
        configProps.put("foo", "bar");
        config.update(configProps);

        ServiceD1 srvD1 = srvD.getServiceD1();
        Assert.assertTrue(srvD1.awaitModified(4000, TimeUnit.MILLISECONDS));

        Assert.assertEquals("ServiceD#1:ServiceD1#1:bar:Hello", srvD.doStuff("Hello"));
    }

    private ConfigurationAdmin getConfigurationAdmin(Module module) {
        ModuleContext context = module.getModuleContext();
        return context.getService(context.getServiceReference(ConfigurationAdmin.class));
    }

    @Deployment(name = BUNDLE_D, managed = false, testable = false)
    public static JavaArchive getBundleD() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, BUNDLE_D);
        archive.addClasses(ServiceD.class);
        archive.addAsResource("OSGI-INF/org.jboss.test.gravia.runtime.osgi.sub.d.ServiceD.xml");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleVersion("1.0.0");
                builder.addExportPackages(ServiceD.class);
                builder.addImportPackages(Runtime.class, ComponentContext.class);
                builder.addImportPackages(ServiceD1.class);
                builder.addManifestHeader(Constants.GRAVIA_IDENTITY_CAPABILITY, BUNDLE_D + ";version=1.0.0");
                builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.runtime.osgi.sub.d.ServiceD.xml");
                return builder.openStream();
            }
        });
        return archive;
    }

    @Deployment(name = BUNDLE_D1, managed = false, testable = false)
    public static JavaArchive getBundleD1() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, BUNDLE_D1);
        archive.addClasses(ServiceD1.class);
        archive.addAsResource("OSGI-INF/org.jboss.test.gravia.runtime.osgi.sub.d1.ServiceD1.xml");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleVersion("1.0.0");
                builder.addImportPackages(Runtime.class, ComponentContext.class);
                builder.addExportPackages(ServiceD1.class);
                builder.addManifestHeader(Constants.GRAVIA_IDENTITY_CAPABILITY, BUNDLE_D1 + ";version=1.0.0");
                builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.runtime.osgi.sub.d1.ServiceD1.xml");
                return builder.openStream();
            }
        });
        return archive;
    }
}
