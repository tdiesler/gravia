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
package org.jboss.test.gravia.runtime.embedded;

import java.util.Dictionary;

import org.jboss.gravia.resource.spi.ManifestBuilder;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.util.ManifestHeadersProvider;
import org.jboss.test.gravia.runtime.embedded.sub.a.EmbeddedServices;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test basic runtime functionality.
 *
 * @author thomas.diesler@jbos.com
 * @since 27-Jan-2012
 */
public class EmbeddedServicesTestCase extends AbstractRuntimeTest {

    static final String MODULE_A = "moduleA";

    @Test
    public void testBasicModule() throws Exception {

        Module modA = getRuntime().installModule(getClass().getClassLoader(), getModuleHeadersA());

        modA.start();

        ModuleContext ctxA = modA.getModuleContext();
        ServiceReference<EmbeddedServices> srefA = ctxA.getServiceReference(EmbeddedServices.class);
        Assert.assertNotNull("ServiceReference not null", srefA);

        EmbeddedServices srvA = ctxA.getService(srefA);
        Assert.assertNotNull(srvA.getConfigurationAdmin());
        Assert.assertNotNull(srvA.getLogService());
        Assert.assertNotNull(srvA.getMBeanServer());
    }

    private Dictionary<String,String> getModuleHeadersA() {
        ManifestBuilder builder = new ManifestBuilder();
        builder.addIdentityCapability(MODULE_A, "1.0.0");
        builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.runtime.embedded.sub.a.EmbeddedServices.xml");
        ManifestHeadersProvider headersProvider = new ManifestHeadersProvider(builder.getManifest());
        return headersProvider.getHeaders();
    }
}
