/*
 * #%L
 * Gravia :: Repository
 * %%
 * Copyright (C) 2012 - 2014 JBoss by Red Hat
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
package org.jboss.test.gravia.repository;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.jboss.gravia.repository.DefaultMavenDelegateRepository;
import org.jboss.gravia.repository.MavenCoordinates;
import org.jboss.gravia.repository.MavenIdentityRequirementBuilder;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.ContentCapability;
import org.jboss.gravia.resource.ContentNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.spi.DefaultPropertiesProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link DefaultMavenDelegateRepository}
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class MavenIdentityRepositoryTestCase extends AbstractRepositoryTest {

    private Repository repository;

    @Before
    public void setUp() throws IOException {
        File storageDir = new File("./target/repository");
        deleteRecursive(storageDir);
        repository = new DefaultMavenDelegateRepository(new DefaultPropertiesProvider());
    }

    @Test
    public void testMavenResource() throws Exception {
        MavenCoordinates mavenid = MavenCoordinates.parse("org.jboss.logging:jboss-logging:3.1.3.GA");
        Requirement req = new MavenIdentityRequirementBuilder(mavenid).getRequirement();
        Collection<Capability> providers = repository.findProviders(req);
        Assert.assertEquals("One provider", 1, providers.size());

        Capability cap = providers.iterator().next();

        Resource res = cap.getResource();
        Assert.assertEquals(ResourceIdentity.fromString("org.jboss.logging.jboss-logging:3.1.3.GA"), res.getIdentity());
        List<Capability> caps = res.getCapabilities(ContentNamespace.CONTENT_NAMESPACE);
        Assert.assertEquals(1, caps.size());

        ContentCapability ccap = (ContentCapability) caps.iterator().next();
        Assert.assertNotNull(ccap.getContentURL());
    }

    @Test
    public void testFindProvidersFails() throws Exception {
        MavenCoordinates mavenid = MavenCoordinates.parse("foo:bar:1.2.8");
        Requirement req = new MavenIdentityRequirementBuilder(mavenid).getRequirement();
        Collection<Capability> caps = repository.findProviders(req);
        Assert.assertEquals("No capability", 0, caps.size());
    }
}
