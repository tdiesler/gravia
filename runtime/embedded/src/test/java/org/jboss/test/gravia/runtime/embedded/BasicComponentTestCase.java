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
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import junit.framework.Assert;

import org.jboss.gravia.runtime.Constants;
import org.jboss.gravia.runtime.Module;
import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.test.gravia.runtime.embedded.suba.SimpleActivator;
import org.junit.Test;

/**
 * Test basic runtime functionality.
 *
 * @author thomas.diesler@jbos.com
 * @since 27-Jan-2012
 */
public class BasicComponentTestCase extends AbstractRuntimeTest {

    @Test
    public void testBasicModule() throws Exception {
        JarFile jarFile = new JarFile("target/test-libs/bundles/org.apache.felix.scr.jar");
        Manifest manifest = jarFile.getManifest();
        OSGiMetaData metaData = OSGiMetaDataBuilder.load(manifest);
        String bundleActivator = metaData.getBundleActivator();

        Map<String, Object> props = new HashMap<String, Object>();
        props.put(Constants.MODULE_MANIFEST, manifest);
        props.put("Bundle-Activator", bundleActivator);
        Module scrModule = getRuntime().installModule(SimpleActivator.class.getClassLoader(), props);
        Assert.assertEquals(Module.State.RESOLVED, scrModule.getState());

        scrModule.start();

        scrModule.stop();
    }
}
