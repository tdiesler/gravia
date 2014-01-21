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

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.spi.ManifestHeadersProvider;
import org.jboss.test.gravia.runtime.embedded.sub.a.ServiceB;
import org.jboss.test.gravia.runtime.embedded.sub.a.ServiceFactoryB;
import org.jboss.test.gravia.runtime.embedded.support.AbstractEmbeddedRuntimeTest;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.component.ComponentFactory;

/**
 * Test component factory
 *
 * @author thomas.diesler@jboss.com
 * @since 06-Nov-2013
 */
public class ComponentFactoryTestCase extends AbstractEmbeddedRuntimeTest {

    static final String MODULE_A = "moduleA";

    @Test
    public void testServiceAccess() throws Exception {

        Module modA = getRuntime().installModule(getClass().getClassLoader(), getModuleHeadersA());
        modA.start();

        ModuleContext contextA = modA.getModuleContext();
        String filter = "(component.factory=" + ServiceFactoryB.FACTORY_ID + ")";
        Collection<ServiceReference<ComponentFactory>> srefs = contextA.getServiceReferences(ComponentFactory.class, filter);
        Assert.assertEquals("ServiceReference not null", 1, srefs.size());

        ComponentFactory factory = contextA.getService(srefs.iterator().next());
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("key", "val1");
        factory.newInstance(props);

        Collection<ServiceReference<ServiceB>> srefsB = contextA.getServiceReferences(ServiceB.class, "(key=val1)");
        Assert.assertEquals("ServiceReference not null", 1, srefsB.size());
        ServiceB srvB = contextA.getService(srefsB.iterator().next());
        Assert.assertEquals("ServiceFactoryB#1:val1", srvB.doStuff());

        props.put("key", "val2");
        factory.newInstance(props);

        srefsB = contextA.getServiceReferences(ServiceB.class, "(key=val2)");
        Assert.assertEquals("ServiceReference not null", 1, srefsB.size());
        srvB = contextA.getService(srefsB.iterator().next());
        Assert.assertEquals("ServiceFactoryB#2:val2", srvB.doStuff());
    }

    private Dictionary<String,String> getModuleHeadersA() {
        ManifestBuilder builder = new ManifestBuilder();
        builder.addIdentityCapability(MODULE_A, "1.0.0");
        builder.addManifestHeader("Service-Component", "OSGI-INF/org.jboss.test.gravia.runtime.embedded.sub.a.ServiceFactoryB.xml");
        ManifestHeadersProvider headersProvider = new ManifestHeadersProvider(builder.getManifest());
        return headersProvider.getHeaders();
    }
}
