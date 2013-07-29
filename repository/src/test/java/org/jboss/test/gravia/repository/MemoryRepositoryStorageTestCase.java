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

import java.util.Collection;

import junit.framework.Assert;

import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.repository.RepositoryStorage;
import org.jboss.gravia.repository.spi.MemoryRepositoryStorage;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultRequirementBuilder;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.RequirementBuilder;
import org.jboss.gravia.resource.Resource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test the {@link MemoryRepositoryStorage}
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class MemoryRepositoryStorageTestCase extends AbstractRepositoryTest {

    private RepositoryStorage storage;

    @Before
    public void setUp() throws Exception {
        storage = new MemoryRepositoryStorage(Mockito.mock(Repository.class));
        RepositoryReader reader = getRepositoryReader("xml/sample-repository.xml");
        storage.addResource(reader.nextResource());
    }

    @Test
    public void testRequireResource() throws Exception {

        RepositoryReader reader = storage.getRepositoryReader();
        Resource resource = reader.nextResource();
        Assert.assertNotNull("Resource not null", resource);
        Assert.assertNull("One resource only", reader.nextResource());

        RequirementBuilder builder = new DefaultRequirementBuilder(IdentityNamespace.IDENTITY_NAMESPACE, "org.acme.pool");
        Requirement req = builder.getRequirement();

        Collection<Capability> providers = storage.findProviders(req);
        Assert.assertNotNull("Providers not null", providers);
        Assert.assertEquals("One provider", 1, providers.size());

        Capability cap = providers.iterator().next();
        Assert.assertNotNull("Capability not null", cap);
        Assert.assertSame(resource, cap.getResource());
    }
}
