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

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.jboss.gravia.runtime.Constants;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.jboss.test.gravia.runtime.embedded.suba.SimpleActivator;
import org.junit.Test;

/**
 * Test basic runtime functionality.
 *
 * @author thomas.diesler@jbos.com
 * @since 27-Jan-2012
 */
public class BasicRuntimeTestCase extends AbstractRuntimeTest {

    @Test
    public void testBasicModule() throws Exception {

        Module moduleA = getRuntime().installModule(SimpleActivator.class.getClassLoader(), null);
        Assert.assertEquals(Module.State.RESOLVED, moduleA.getState());

        moduleA.start();
        Assert.assertEquals(Module.State.ACTIVE, moduleA.getState());

        ModuleContext context = moduleA.getModuleContext();
        ServiceRegistration<String> sreg = context.registerService(String.class, new String("Hello"), null);
        Assert.assertNotNull("Null sreg", sreg);

        String service = context.getService(sreg.getReference());
        Assert.assertEquals("Hello", service);

        moduleA.stop();
        Assert.assertEquals(Module.State.RESOLVED, moduleA.getState());

        moduleA.uninstall();
        Assert.assertEquals(Module.State.UNINSTALLED, moduleA.getState());

        try {
            moduleA.start();
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    @Test
    public void testBundleWithClassLoader() throws Exception {

        Map<String, Object> props = new HashMap<String, Object>();
        props.put(Constants.MODULE_ACTIVATOR, SimpleActivator.class.getName());
        Module moduleA = getRuntime().installModule(SimpleActivator.class.getClassLoader(), props);
        Assert.assertEquals(Module.State.RESOLVED, moduleA.getState());

        ModuleContext context = moduleA.getModuleContext();
        Assert.assertNull("Null moduleContext", context);

        moduleA.start();
        Assert.assertEquals(Module.State.ACTIVE, moduleA.getState());

        context = moduleA.getModuleContext();
        ServiceReference<String> sref = context.getServiceReference(String.class);
        Assert.assertNotNull("Null sref", sref);

        String service = context.getService(sref);
        Assert.assertEquals("Hello", service);

        moduleA.stop();
        Assert.assertEquals(Module.State.RESOLVED, moduleA.getState());

        moduleA.uninstall();
        Assert.assertEquals(Module.State.UNINSTALLED, moduleA.getState());

        try {
            moduleA.start();
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ex) {
            // expected
        }
    }
}
