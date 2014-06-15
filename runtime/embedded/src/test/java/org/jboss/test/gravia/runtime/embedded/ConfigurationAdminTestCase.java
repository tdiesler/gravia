/*
 * #%L
 * Gravia :: Runtime :: Embedded
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
package org.jboss.test.gravia.runtime.embedded;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceLocator;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.spi.ManifestHeadersProvider;
import org.jboss.test.gravia.runtime.embedded.sub.d.ServiceD;
import org.jboss.test.gravia.runtime.embedded.sub.d1.ServiceD1;
import org.jboss.test.gravia.runtime.embedded.support.AbstractEmbeddedRuntimeTest;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Test static service references with configuration
 *
 * @author thomas.diesler@jboss.com
 * @since 11-Sep-2013
 */
public class ConfigurationAdminTestCase extends AbstractEmbeddedRuntimeTest {

    static final String MODULE_D = "moduleD";
    static final String MODULE_D1 = "moduleD1";

    @Test
    public void testServiceAccess() throws Exception {

        Module modD = getRuntime().installModule(getClass().getClassLoader(), getModuleHeadersD());
        modD.start();

        Module modD1 = getRuntime().installModule(getClass().getClassLoader(), getModuleHeadersD1());
        modD1.start();

        ModuleContext contextD = modD.getModuleContext();
        ServiceReference<ServiceD> srefD = contextD.getServiceReference(ServiceD.class);
        Assert.assertNotNull("ServiceReference not null", srefD);

        ServiceD srvD = contextD.getService(srefD);
        Assert.assertEquals("ServiceD#1:ServiceD1#1:null:Hello", srvD.doStuff("Hello"));

        ConfigurationAdmin configAdmin = ServiceLocator.getRequiredService(modD1.getModuleContext(), ConfigurationAdmin.class);
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

    private Dictionary<String,String> getModuleHeadersD() {
        ManifestBuilder builder = new ManifestBuilder();
        builder.addIdentityCapability(MODULE_D, "1.0.0");
        builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.runtime.embedded.sub.d.ServiceD.xml");
        ManifestHeadersProvider headersProvider = new ManifestHeadersProvider(builder.getManifest());
        return headersProvider.getHeaders();
    }

    private Dictionary<String,String> getModuleHeadersD1() {
        ManifestBuilder builder = new ManifestBuilder();
        builder.addIdentityCapability(MODULE_D1, "1.0.0");
        builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.runtime.embedded.sub.d1.ServiceD1.xml");
        ManifestHeadersProvider headersProvider = new ManifestHeadersProvider(builder.getManifest());
        return headersProvider.getHeaders();
    }
}
