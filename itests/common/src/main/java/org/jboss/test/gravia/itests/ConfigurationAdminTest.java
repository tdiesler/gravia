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
package org.jboss.test.gravia.itests;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.test.gravia.itests.sub.d.ServiceD;
import org.jboss.test.gravia.itests.sub.d1.ServiceD1;
import org.jboss.test.gravia.itests.support.HttpRequest;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Test basic SCR Component
 *
 * @author thomas.diesler@jbos.com
 * @since 04-Oct-2013
 */
public abstract class ConfigurationAdminTest  {

    @Test
    public void testBasicModule() throws Exception {

        String result = performCall("/configadmin-test/servlet?input=Hello");
        Assert.assertEquals("Hello from Module[configadmin-test:1.0.0]", result);

        ResourceIdentity identity = ResourceIdentity.fromString("configadmin-test:1.0.0");
        Runtime runtime = RuntimeLocator.getRuntime();
        Module module = runtime.getModule(identity);

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

    protected String performCall(String path) throws Exception {
        String urlspec = "http://localhost:8080" + path;
        return HttpRequest.get(urlspec, 10, TimeUnit.SECONDS);
    }

    protected ConfigurationAdmin getConfigurationAdmin(Module module) {
        ModuleContext context = module.getModuleContext();
        return context.getService(context.getServiceReference(ConfigurationAdmin.class));
    }
}
