/*
 * #%L
 * Gravia :: Resource
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
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
package org.jboss.test.gravia.resource;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.gravia.Constants;
import org.jboss.gravia.resource.ManifestBuilder;
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
