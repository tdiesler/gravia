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
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.osgi.StartLevelAware;
import org.jboss.gravia.itests.support.ArchiveBuilder;
import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.RuntimeType;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.test.gravia.itests.sub.d.ServiceD;
import org.jboss.test.gravia.itests.sub.d1.ServiceD1;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;

/**
 * Test basic SCR Component
 *
 * @author thomas.diesler@jboss.com
 * @since 04-Oct-2013
 */
@RunWith(Arquillian.class)
public class ConfigurationAdminTest  {

    @Deployment
    @StartLevelAware(autostart = true)
    public static Archive<?> deployment() {
        final ArchiveBuilder archive = new ArchiveBuilder("configadmin-test");
        archive.addClasses(ConfigurationAdminTest.class);
        archive.addClasses(ServiceD.class, ServiceD1.class);
        archive.addAsResource("OSGI-INF/org.jboss.test.gravia.itests.sub.d.ServiceD.xml");
        archive.addAsResource("OSGI-INF/org.jboss.test.gravia.itests.sub.d1.ServiceD1.xml");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                if (ArchiveBuilder.getTargetContainer() == RuntimeType.KARAF) {
                    OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                    builder.addBundleManifestVersion(2);
                    builder.addBundleSymbolicName(archive.getName());
                    builder.addBundleVersion("1.0.0");
                    builder.addImportPackages(RuntimeLocator.class, ComponentContext.class, ConfigurationAdmin.class, Resource.class);
                    builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.itests.sub.d.ServiceD.xml,OSGI-INF/org.jboss.test.gravia.itests.sub.d1.ServiceD1.xml");
                    return builder.openStream();
                } else {
                    ManifestBuilder builder = new ManifestBuilder();
                    builder.addIdentityCapability(archive.getName(), "1.0.0");
                    builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.itests.sub.d.ServiceD.xml,OSGI-INF/org.jboss.test.gravia.itests.sub.d1.ServiceD1.xml");
                    return builder.openStream();
                }
            }
        });
        return archive.getArchive();
    }

    @Test
    public void testBasicModule() throws Exception {

        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Module module = runtime.getModule(getClass().getClassLoader());

        ModuleContext ctxA = module.getModuleContext();
        ServiceReference<ServiceD> srefD = ctxA.getServiceReference(ServiceD.class);
        Assert.assertNotNull("ServiceReference not null", srefD);

        ServiceD srvD = ctxA.getService(srefD);
        Assert.assertEquals("ServiceD#1:ServiceD1#1:null:Hello", srvD.doStuff("Hello"));

        ConfigurationAdmin configAdmin = getConfigurationAdmin(module);
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

    protected ConfigurationAdmin getConfigurationAdmin(Module module) {
        ModuleContext context = module.getModuleContext();
        return context.getService(context.getServiceReference(ConfigurationAdmin.class));
    }
}
