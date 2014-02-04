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

import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.spi.ManifestHeadersProvider;
import org.jboss.test.gravia.runtime.embedded.sub.a.ServiceA;
import org.jboss.test.gravia.runtime.embedded.support.AbstractEmbeddedRuntimeTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test basic runtime functionality.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Jan-2012
 */
public class ServiceComponentTestCase extends AbstractEmbeddedRuntimeTest {

    static final String MODULE_A = "moduleA";
    static final String MODULE_A1 = "moduleA1";

    @Test
    public void testBasicModule() throws Exception {

        Module modA = getRuntime().installModule(getClass().getClassLoader(), getModuleHeadersA());
        Module modA1 = getRuntime().installModule(getClass().getClassLoader(), getModuleHeadersA1());

        modA.start();
        modA1.start();

        ModuleContext ctxA = modA.getModuleContext();
        ServiceReference<ServiceA> srefA = ctxA.getServiceReference(ServiceA.class);
        Assert.assertNotNull("ServiceReference not null", srefA);

        ServiceA srvA = ctxA.getService(srefA);
        Assert.assertEquals("ServiceA#1:ServiceA1#1:Hello", srvA.doStuff("Hello"));
    }

    private Dictionary<String,String> getModuleHeadersA() {
        ManifestBuilder builder = new ManifestBuilder();
        builder.addIdentityCapability(MODULE_A, "1.0.0");
        builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.runtime.embedded.sub.a.ServiceA.xml");
        ManifestHeadersProvider headersProvider = new ManifestHeadersProvider(builder.getManifest());
        return headersProvider.getHeaders();
    }

    private Dictionary<String,String> getModuleHeadersA1() {
        ManifestBuilder builder = new ManifestBuilder();
        builder.addIdentityCapability(MODULE_A1, "1.0.0");
        builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.runtime.embedded.sub.a1.ServiceA1.xml");
        ManifestHeadersProvider headersProvider = new ManifestHeadersProvider(builder.getManifest());
        return headersProvider.getHeaders();
    }
}
