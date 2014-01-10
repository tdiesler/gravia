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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.jboss.gravia.Constants;
import org.jboss.gravia.repository.DefaultRepositoryStorage;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.repository.RepositoryStorage;
import org.jboss.gravia.repository.spi.RepositoryContentHelper;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.ContentCapability;
import org.jboss.gravia.resource.ContentNamespace;
import org.jboss.gravia.resource.DefaultRequirementBuilder;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.IdentityRequirementBuilder;
import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceContent;
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
 * Test the {@link DefaultRepositoryStorage}
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class PersistentRepositoryStorageTestCase extends AbstractRepositoryTest {

    private File storageDir;
    private RepositoryStorage storage;
    private Repository repository;
    private PropertiesProvider propertyProvider;
    private File resAjar;
    private File resAtxt;

    @Before
    public void setUp() throws Exception {
        storageDir = new File("./target/repository/" + System.currentTimeMillis()).getCanonicalFile();
        repository = Mockito.mock(Repository.class);
        Mockito.when(repository.getName()).thenReturn("MockedRepo");
        propertyProvider = Mockito.mock(PropertiesProvider.class);
        Mockito.when(propertyProvider.getProperty(Constants.PROPERTY_REPOSITORY_STORAGE_DIR, null)).thenReturn(storageDir.getPath());
        storage = new DefaultRepositoryStorage(propertyProvider, repository);

        // Write the bundle to the location referenced by repository-testA.xml
        resAjar = new File("./target/resA.jar");
        getResourceA().as(ZipExporter.class).exportTo(resAjar, true);

        // Write some text to the location referenced by repository-testB.xml
        resAtxt = new File("./target/resA.txt");
        PrintWriter bw = new PrintWriter(new FileWriter(resAtxt));
        bw.print("some text");
        bw.close();
    }

    @After
    public void tearDown() {
        deleteRecursive(storageDir);
        resAjar.delete();
        resAtxt.delete();
    }

    @Test
    public void testAddResourceFromXML() throws Exception {
        RepositoryReader reader = getRepositoryReader("xml/repository-testA.xml");
        Resource resource = storage.addResource(reader.nextResource());
        verifyResource(resource);
        verifyProviders(storage);
    }

    @Test
    public void testAddResourceWithMultipleContent() throws Exception {
        // Add a resource from XML
        RepositoryReader reader = getRepositoryReader("xml/repository-testB.xml");
        storage.addResource(reader.nextResource());

        Requirement req = new DefaultRequirementBuilder(IdentityNamespace.IDENTITY_NAMESPACE, "resA").getRequirement();
        Collection<Capability> providers = storage.findProviders(req);
        Assert.assertNotNull(providers);
        Assert.assertEquals(1, providers.size());

        Capability cap = providers.iterator().next();
        Assert.assertEquals("resA", cap.getAttribute(IdentityNamespace.IDENTITY_NAMESPACE));

        Resource resource = cap.getResource();
        verifyDefaultContent(resource);

        InputStream input = resource.adapt(ResourceContent.class).getContent();
        String digest = RepositoryContentHelper.getDigest(input);
        Assert.assertNotNull("RepositoryContent not null", input);
        input.close();

        List<Capability> ccaps = resource.getCapabilities(ContentNamespace.CONTENT_NAMESPACE);
        Assert.assertEquals(2, ccaps.size());
        ContentCapability ccap = ccaps.get(0).adapt(ContentCapability.class);
        Assert.assertEquals(digest, ccap.getDigest());
        Assert.assertEquals("application/java-archive", ccap.getMimeType());
        Assert.assertEquals(new Long(392), ccap.getSize());
        File contentFile = new File(ccap.getContentURL().getPath()).getCanonicalFile();
        Assert.assertTrue("File exists: " + contentFile, contentFile.exists());
        Assert.assertTrue("Path starts with: " + storageDir.getPath(), contentFile.getPath().startsWith(storageDir.getPath()));

        ccap = ccaps.get(1).adapt(ContentCapability.class);
        Assert.assertFalse(digest.equals(ccap.getDigest()));
        Assert.assertEquals("text/plain", ccap.getMimeType());
        Assert.assertEquals(new Long("some text".length()), ccap.getSize());
        contentFile = new File(ccap.getContentURL().getPath()).getCanonicalFile();
        Assert.assertTrue("File exists: " + contentFile, contentFile.exists());
        Assert.assertTrue("Path starts with: " + storageDir.getPath(), contentFile.getPath().startsWith(storageDir.getPath()));

        BufferedReader br = new BufferedReader(new FileReader(contentFile));
        Assert.assertEquals("some text", br.readLine());
        br.close();
    }

    @Test
    public void testFileStorageRestart() throws Exception {
        RepositoryReader reader = getRepositoryReader("xml/repository-testA.xml");
        Resource resource = storage.addResource(reader.nextResource());

        verifyResource(resource);
        verifyProviders(storage);

        RepositoryStorage other = new DefaultRepositoryStorage(propertyProvider, repository);
        verifyProviders(other);
    }

    @Test
    public void testCustomNamespace() throws Exception {

        // Add a resource from XML
        RepositoryReader reader = getRepositoryReader("xml/repository-testA.xml");
        Resource resource = storage.addResource(reader.nextResource());

        verifyResource(resource);
        verifyProviders(storage);

        List<Capability> allcaps = resource.getCapabilities(null);
        Assert.assertEquals(3, allcaps.size());

        Requirement req = new DefaultRequirementBuilder("custom.namespace", "custom.value").getRequirement();
        Collection<Capability> providers = storage.findProviders(req);
        Assert.assertEquals("One provider", 1, providers.size());

        req = new DefaultRequirementBuilder("custom.namespace", "bogus").getRequirement();
        providers = storage.findProviders(req);
        Assert.assertEquals("No provider", 0, providers.size());
    }

    private void verifyResource(Resource resource) throws Exception {
        verifyDefaultContent(resource);
        Assert.assertEquals(3, resource.getCapabilities(null).size());
    }

    private void verifyDefaultContent(Resource resource) throws Exception {
        InputStream input = resource.adapt(ResourceContent.class).getContent();
        String digest = RepositoryContentHelper.getDigest(input);
        Assert.assertNotNull("RepositoryContent not null", input);
        input.close();

        Capability cap = resource.getCapabilities(ContentNamespace.CONTENT_NAMESPACE).get(0);
        ContentCapability ccap = cap.adapt(ContentCapability.class);
        Assert.assertEquals(digest, ccap.getDigest());
        Assert.assertEquals(digest, cap.getAttribute(ContentNamespace.CONTENT_NAMESPACE));
        Assert.assertEquals("application/java-archive", ccap.getMimeType());
        Assert.assertEquals("application/java-archive", cap.getAttribute(ContentNamespace.CAPABILITY_MIME_ATTRIBUTE));
        Assert.assertEquals(new Long(392), ccap.getSize());
        Assert.assertEquals(new Long(392), cap.getAttribute(ContentNamespace.CAPABILITY_SIZE_ATTRIBUTE));
        URL contentURL = (URL) ccap.getAttribute(ContentNamespace.CAPABILITY_URL_ATTRIBUTE);
        File contentFile = new File(contentURL.getPath()).getCanonicalFile();
        Assert.assertTrue("File exists: " + contentFile, contentFile.exists());
        Assert.assertTrue("Path starts with: " + storageDir.getPath(), contentFile.getPath().startsWith(storageDir.getPath()));
    }

    private void verifyProviders(RepositoryStorage storage) throws Exception {
        Requirement req = new IdentityRequirementBuilder("resA", "[1.0,2.0)").getRequirement();
        Collection<Capability> providers = storage.findProviders(req);
        Assert.assertNotNull(providers);
        Assert.assertEquals(1, providers.size());

        Capability cap = providers.iterator().next();
        Assert.assertEquals("resA", cap.getAttribute(IdentityNamespace.IDENTITY_NAMESPACE));

        verifyResource(cap.getResource());
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
