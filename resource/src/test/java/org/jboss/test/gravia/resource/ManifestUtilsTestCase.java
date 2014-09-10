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
import java.io.InputStream;
import java.util.jar.Manifest;

import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.utils.ManifestUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the manifest utils.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Jan-2012
 */
public class ManifestUtilsTestCase {

    @Test
    public void testManifestUtils() throws IOException {
        InputStream input = getArchive().as(ZipExporter.class).exportAsInputStream();
        Manifest manifest = ManifestUtils.getManifest(input);
        Assert.assertNotNull(manifest);
    }

    public static Archive<?> getArchive() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "some.jar");
        archive.addClasses(ManifestUtilsTestCase.class);
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                ManifestBuilder builder = new ManifestBuilder();
                builder.addIdentityCapability("some.id", Version.emptyVersion);
                return builder.openStream();
            }
        });
        return archive;
    }
}
