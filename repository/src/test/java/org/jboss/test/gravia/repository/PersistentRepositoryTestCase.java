package org.jboss.test.gravia.repository;

/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2012 - 2013 JBoss by Red Hat
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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.jboss.gravia.Constants;
import org.jboss.gravia.repository.DefaultRepository;
import org.jboss.gravia.repository.MavenCoordinates;
import org.jboss.gravia.repository.MavenIdentityRequirementBuilder;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryStorage;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.ContentCapability;
import org.jboss.gravia.resource.ContentNamespace;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.IdentityRequirementBuilder;
import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.ResourceContent;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test the {@link DefaultRepository}
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class PersistentRepositoryTestCase extends AbstractRepositoryTest {

    private Repository repository;
    private File storageDir;
    private File resAjar;

    @Before
    public void setUp() throws IOException {
        storageDir = new File("./target/repository");
        deleteRecursive(storageDir);
        PropertiesProvider propertyProvider = Mockito.mock(PropertiesProvider.class);
        Mockito.when(propertyProvider.getProperty(Constants.PROPERTY_REPOSITORY_STORAGE_DIR, null)).thenReturn(storageDir.getPath());
        repository = new DefaultRepository(propertyProvider);

        // Write the bundle to the location referenced by repository-testA.xml
        resAjar = new File("./target/resA.jar");
        getResourceA().as(ZipExporter.class).exportTo(resAjar, true);
    }

    @After
    public void tearDown() {
        deleteRecursive(storageDir);
        resAjar.delete();
    }

    @Test
    public void testFindProvidersByMavenId() throws Exception {

        MavenCoordinates mavenid = MavenCoordinates.parse("org.jboss.logging:jboss-logging:3.1.3.GA");
        Requirement req = new MavenIdentityRequirementBuilder(mavenid).getRequirement();
        Collection<Capability> providers = repository.findProviders(req);
        Assert.assertEquals("One provider", 1, providers.size());

        // Verify that the resource is in storage
        req = new IdentityRequirementBuilder("org.jboss.logging.jboss-logging", "3.1.3.GA").getRequirement();
        providers = repository.findProviders(req);
        Assert.assertEquals("One provider", 1, providers.size());

        // Verify the content capability
        Resource res = providers.iterator().next().getResource();
        assertEquals(ResourceIdentity.fromString("org.jboss.logging.jboss-logging:3.1.3.GA"), res.getIdentity());

        verifyResourceContent(res);

        repository.removeResource(res.getIdentity());
    }

    @Test
    public void testAddResourceWithMavenId() throws Exception {

        ResourceBuilder builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("org.jboss.logging", "3.1.3.GA");
        Resource res = builder.getResource();

        MavenCoordinates mavenid = MavenCoordinates.parse("org.jboss.logging:jboss-logging:3.1.3.GA");
        res = repository.addResource(res, mavenid);

        Requirement req = new IdentityRequirementBuilder("org.jboss.logging", "[3.1,4.0)").getRequirement();
        Collection<Capability> providers = repository.findProviders(req);
        Assert.assertEquals("One provider", 1, providers.size());

        // Verify that the resource is in storage
        providers = repository.adapt(RepositoryStorage.class).findProviders(req);
        Assert.assertEquals("One provider", 1, providers.size());

        // Verify the content capability
        res = providers.iterator().next().getResource();
        assertEquals(ResourceIdentity.fromString("org.jboss.logging:3.1.3.GA"), res.getIdentity());

        verifyResourceContent(res);

        repository.removeResource(res.getIdentity());
    }

    @Test
    public void testAddResourceFromURL() throws Exception {

        ResourceBuilder builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resA", "1.0.0");
        builder.addContentCapability(resAjar.toURI().toURL());
        Resource res = builder.getResource();

        res = repository.addResource(res);

        Requirement req = new IdentityRequirementBuilder("resA", "[1.0,2.0)").getRequirement();
        Collection<Capability> providers = repository.findProviders(req);
        Assert.assertEquals("One provider", 1, providers.size());

        // Verify that the resource is in storage
        providers = repository.adapt(RepositoryStorage.class).findProviders(req);
        Assert.assertEquals("One provider", 1, providers.size());

        // Verify the content capability
        res = providers.iterator().next().getResource();
        assertEquals(ResourceIdentity.fromString("resA:1.0.0"), res.getIdentity());

        verifyResourceContent(res);

        repository.removeResource(res.getIdentity());
    }

    @Test
    public void testAddResourceFromInputStream() throws Exception {

        ResourceBuilder builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resA", "1.0.0");
        builder.addContentCapability(getResourceA().as(ZipExporter.class).exportAsInputStream());
        Resource res = builder.getResource();

        res = repository.addResource(res);

        Requirement req = new IdentityRequirementBuilder("resA", "[1.0,2.0)").getRequirement();
        Collection<Capability> providers = repository.findProviders(req);
        Assert.assertEquals("One provider", 1, providers.size());

        // Verify that the resource is in storage
        providers = repository.adapt(RepositoryStorage.class).findProviders(req);
        Assert.assertEquals("One provider", 1, providers.size());

        // Verify the content capability
        res = providers.iterator().next().getResource();
        assertEquals(ResourceIdentity.fromString("resA:1.0.0"), res.getIdentity());

        verifyResourceContent(res);

        repository.removeResource(res.getIdentity());
    }

    private void verifyResourceContent(Resource res) throws Exception {

        Collection<Capability> caps = res.getCapabilities(ContentNamespace.CONTENT_NAMESPACE);
        assertEquals("One capability", 1, caps.size());
        ContentCapability ccap = (ContentCapability) caps.iterator().next();
        URL contentURL = ccap.getContentURL();

        URL baseURL = storageDir.toURI().toURL();
        Assert.assertTrue("Local path: " + contentURL, contentURL.getPath().startsWith(baseURL.getPath()));

        ResourceContent content = res.adapt(ResourceContent.class);
        JarInputStream jarstream = new JarInputStream(content.getContent());
        try {
            Manifest manifest = jarstream.getManifest();
            Assert.assertNotNull(manifest);
        } finally {
            jarstream.close();
        }
    }

    private JavaArchive getResourceA() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "resA");
        archive.setManifest(new Asset() {
            @Override
            public InputStream openStream() {
                ManifestBuilder builder = new ManifestBuilder();
                builder.addIdentityCapability("resA", "1.0.0");
                builder.addIdentityRequirement("resB", "[1.0,2.0)");
                builder.addGenericCapabilities("custom.namespace;custom.namespace=custom.value");
                return builder.openStream();
            }
        });
        return archive;
    }
}
