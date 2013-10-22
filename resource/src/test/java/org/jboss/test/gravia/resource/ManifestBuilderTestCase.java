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
package org.jboss.test.gravia.resource;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.gravia.resource.Constants;
import org.jboss.gravia.resource.spi.ManifestBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the simple manifest builder.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Jan-2012
 */
public class ManifestBuilderTestCase {

    @Test
    public void testBasicManifest() throws IOException {

        ManifestBuilder builder = new ManifestBuilder();
        builder.addManifestHeader(Constants.GRAVIA_IDENTITY_CAPABILITY, "org.acme.foo;version=1.0.0");
        Manifest manifest = builder.getManifest();
        Assert.assertNotNull("Manifest not null", manifest);

        Attributes attributes = manifest.getMainAttributes();
        String value = attributes.getValue(Constants.GRAVIA_IDENTITY_CAPABILITY);
        Assert.assertEquals("org.acme.foo;version=1.0.0", value);
    }
}
