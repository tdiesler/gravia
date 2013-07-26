package org.jboss.test.gravia.repository;

/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import junit.framework.Assert;

import org.jboss.gravia.repository.ContentCapability;
import org.jboss.gravia.repository.ContentNamespace;
import org.jboss.gravia.repository.DefaultMavenIdentityRepository;
import org.jboss.gravia.repository.DefaultPersistentRepository;
import org.jboss.gravia.repository.MavenCoordinates;
import org.jboss.gravia.repository.MavenIdentityRequirementBuilder;
import org.jboss.gravia.repository.PersistentRepository;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.Repository.ConfigurationPropertyProvider;
import org.jboss.gravia.repository.RepositoryContent;
import org.jboss.gravia.repository.RepositoryStorage;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.IdentityRequirementBuilder;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test the {@link DefaultPersistentRepository}
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class PersistentRepositoryTestCase extends AbstractRepositoryTest {

    private PersistentRepository repository;
    private File storageDir;

    @Before
    public void setUp() throws IOException {
        storageDir = new File("./target/repository");
        deleteRecursive(storageDir);
        ConfigurationPropertyProvider propertyProvider = Mockito.mock(ConfigurationPropertyProvider.class);
        Mockito.when(propertyProvider.getProperty(Repository.PROPERTY_REPOSITORY_STORAGE_DIR, null)).thenReturn(storageDir.getPath());
        repository = new DefaultPersistentRepository(propertyProvider, new DefaultMavenIdentityRepository(propertyProvider));
    }

    @Test
    public void testFindProvidersByMavenId() throws Exception {

        MavenCoordinates mavenid = MavenCoordinates.parse("org.jboss.logging:jboss-logging:3.1.3.GA");
        Requirement req = new MavenIdentityRequirementBuilder(mavenid).getRequirement();
        Collection<Capability> providers = repository.findProviders(req);
        Assert.assertEquals("One provider", 1, providers.size());

        RepositoryStorage storage = repository.adapt(RepositoryStorage.class);

        // Verify that the resource is in storage
        req = new IdentityRequirementBuilder("org.jboss.logging.jboss-logging", "3.1.3.GA").getRequirement();
        providers = storage.findProviders(req);
        Assert.assertEquals("One provider", 1, providers.size());

        // Verify the content capability
        Resource res = providers.iterator().next().getResource();
        assertEquals(ResourceIdentity.fromString("org.jboss.logging.jboss-logging:3.1.3.GA"), res.getIdentity());

        verifyResourceContent(res);

        storage.removeResource(res.getIdentity());
    }

    @Test
    public void testAddResourceWithMavenId() throws Exception {

        DefaultResourceBuilder builder = new DefaultResourceBuilder();
        Capability icap = builder.addIdentityCapability("org.jboss.logging", "3.1.3.GA");
        MavenCoordinates mavenid = MavenCoordinates.parse("org.jboss.logging:jboss-logging:3.1.3.GA");
        icap.getAttributes().put(IdentityNamespace.CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE, mavenid.toExternalForm());
        Resource res = builder.getResource();

        RepositoryStorage storage = repository.adapt(RepositoryStorage.class);
        res = storage.addResource(res);

        Requirement req = new IdentityRequirementBuilder("org.jboss.logging", "[3.1,4.0)").getRequirement();
        Collection<Capability> providers = repository.findProviders(req);
        Assert.assertEquals("One provider", 1, providers.size());

        // Verify that the resource is in storage
        providers = storage.findProviders(req);
        Assert.assertEquals("One provider", 1, providers.size());

        // Verify the content capability
        res = providers.iterator().next().getResource();
        assertEquals(ResourceIdentity.fromString("org.jboss.logging:3.1.3.GA"), res.getIdentity());

        verifyResourceContent(res);

        storage.removeResource(res.getIdentity());
    }

    private void verifyResourceContent(Resource res) throws Exception {

        Collection<Capability> caps = res.getCapabilities(ContentNamespace.CONTENT_NAMESPACE);
        assertEquals("One capability", 1, caps.size());
        ContentCapability ccap = (ContentCapability) caps.iterator().next();
        URL contentURL = new URL(ccap.getContentURL());

        URL baseURL = storageDir.toURI().toURL();
        Assert.assertTrue("Local path: " + contentURL, contentURL.getPath().startsWith(baseURL.getPath()));

        RepositoryContent content = (RepositoryContent) res;
        JarInputStream jarstream = new JarInputStream(content.getContent());
        try {
            Manifest manifest = jarstream.getManifest();
            Assert.assertNotNull(manifest);
        } finally {
            jarstream.close();
        }
    }
}