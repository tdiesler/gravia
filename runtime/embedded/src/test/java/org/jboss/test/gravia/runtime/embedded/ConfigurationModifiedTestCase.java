/*
 * #%L
 * Gravia :: Runtime :: Embedded
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
public class ConfigurationModifiedTestCase extends AbstractRuntimeTest {

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