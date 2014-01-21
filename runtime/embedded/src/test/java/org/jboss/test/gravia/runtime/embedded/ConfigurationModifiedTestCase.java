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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceEvent;
import org.jboss.gravia.runtime.ServiceListener;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.util.ManifestHeadersProvider;
import org.jboss.test.gravia.runtime.embedded.sub.a.ServiceC;
import org.jboss.test.gravia.runtime.embedded.support.AbstractEmbeddedRuntimeTest;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Test configuration modified
 *
 * @author thomas.diesler@jboss.com
 * @since 06-Nov-2013
 */
public class ConfigurationModifiedTestCase extends AbstractEmbeddedRuntimeTest {

    static final String MODULE_C = "moduleC";

    AtomicReference<CountDownLatch> latchref = new AtomicReference<CountDownLatch>();

    @Test
    public void testServiceAccess() throws Exception {

        Module modC = getRuntime().installModule(getClass().getClassLoader(), getModuleHeadersD());
        modC.start();

        ModuleContext contextC = modC.getModuleContext();
        ServiceReference<ServiceC> srefC = contextC.getServiceReference(ServiceC.class);
        Assert.assertNotNull("ServiceReference not null", srefC);

        ServiceC srvC = contextC.getService(srefC);
        Assert.assertEquals("ServiceC#1:null", srvC.doStuff());

        latchref.set(new CountDownLatch(1));
        ServiceListener listener = new ServiceListener() {
            @Override
            public void serviceChanged(ServiceEvent event) {
                if (event.getType() == ServiceEvent.MODIFIED) {
                    latchref.get().countDown();
                }
            }
        };
        contextC.addServiceListener(listener);

        ConfigurationAdmin configAdmin = getConfigurationAdmin(modC);
        Configuration config = configAdmin.getConfiguration(ServiceC.PID, null);
        Dictionary<String, Object> props = config.getProperties();
        Assert.assertNull("Config is empty, but was: " + props, props);
        props = new Hashtable<String, Object>();
        props.put("key", "val1");
        config.update(props);

        Assert.assertTrue("Service modified", latchref.get().await(500, TimeUnit.MILLISECONDS));

        srefC = contextC.getServiceReference(ServiceC.class);
        Assert.assertNotNull("ServiceReference not null", srefC);
        srvC = contextC.getService(srefC);
        Assert.assertEquals("ServiceC#1:val1", srvC.doStuff());

        latchref.set(new CountDownLatch(1));

        props = config.getProperties();
        Assert.assertNotNull("Config not empty, but was: " + props, props);
        props.put("key", "val2");
        config.update(props);

        Assert.assertTrue("Service modified", latchref.get().await(500, TimeUnit.MILLISECONDS));

        srefC = contextC.getServiceReference(ServiceC.class);
        Assert.assertNotNull("ServiceReference not null", srefC);
        srvC = contextC.getService(srefC);
        Assert.assertEquals("ServiceC#1:val2", srvC.doStuff());
    }

    private Dictionary<String,String> getModuleHeadersD() {
        ManifestBuilder builder = new ManifestBuilder();
        builder.addIdentityCapability(MODULE_C, "1.0.0");
        builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.runtime.embedded.sub.a.ServiceC.xml");
        ManifestHeadersProvider headersProvider = new ManifestHeadersProvider(builder.getManifest());
        return headersProvider.getHeaders();
    }
}
