/*
 * #%L
 * JBossOSGi SPI
 * %%
 * Copyright (C) 2010 - 2013 JBoss by Red Hat
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
package org.jboss.test.gravia.runtime;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.jboss.gravia.runtime.Constants;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.Runtime.RuntimeFactory;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.gravia.runtime.suba.SimpleActivator;
import org.junit.Test;
import org.osgi.framework.ServiceReference;

/**
 * Test basic runtime functionality.
 *
 * @author thomas.diesler@jbos.com
 * @since 27-Jan-2012
 */
public class BasicRuntimeTestCase {

    @Test
    public void testBasicModule() throws Exception {

        Runtime runtime = RuntimeFactory.createRuntime();
        Module moduleA = runtime.installModule(SimpleActivator.class.getClassLoader(), null);
        Assert.assertEquals(Module.State.RESOLVED, moduleA.getState());

        moduleA.start();
        Assert.assertEquals(Module.State.ACTIVE, moduleA.getState());

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

        Runtime runtime = RuntimeFactory.createRuntime();

        Map<String, Object> props = new HashMap<String, Object>();
        props.put(Constants.MODULE_ACTIVATOR, SimpleActivator.class.getName());
        props.put(Constants.MODULE_TYPE, Module.Type.BUNDLE);
        Module moduleA = runtime.installModule(SimpleActivator.class.getClassLoader(), props);
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

    private JavaArchive getBundleA() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "bundleA");
        archive.addClasses(SimpleActivator.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleVersion("1.0.0");
                builder.addBundleActivator(SimpleActivator.class);
                builder.addImportPackages("org.osgi.framework");
                return builder.openStream();
            }
        });
        return archive;
    }
}
