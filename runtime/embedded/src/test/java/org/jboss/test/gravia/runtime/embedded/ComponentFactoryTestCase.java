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

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.util.ManifestHeadersProvider;
import org.jboss.test.gravia.runtime.embedded.sub.a.ServiceB;
import org.jboss.test.gravia.runtime.embedded.sub.a.ServiceFactoryB;
import org.jboss.test.gravia.runtime.embedded.support.AbstractRuntimeTest;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.component.ComponentFactory;

/**
 * Test component factory
 *
 * @author thomas.diesler@jboss.com
 * @since 06-Nov-2013
 */
public class ComponentFactoryTestCase extends AbstractRuntimeTest {

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