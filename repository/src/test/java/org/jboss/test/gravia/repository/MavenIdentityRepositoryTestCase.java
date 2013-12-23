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
package org.jboss.test.gravia.repository;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.jboss.gravia.repository.ContentCapability;
import org.jboss.gravia.repository.ContentNamespace;
import org.jboss.gravia.repository.DefaultMavenDelegateRepository;
import org.jboss.gravia.repository.MavenCoordinates;
import org.jboss.gravia.repository.MavenIdentityRequirementBuilder;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.util.DefaultPropertiesProvider;
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
