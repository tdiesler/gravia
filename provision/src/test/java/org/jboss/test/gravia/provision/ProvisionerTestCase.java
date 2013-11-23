/*
 * #%L
 * JBossOSGi Provision: Core
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
package org.jboss.test.gravia.provision;

import java.util.Collections;
import java.util.Iterator;

import org.jboss.gravia.provision.Environment;
import org.jboss.gravia.provision.ProvisionResult;
import org.jboss.gravia.provision.Provisioner;
import org.jboss.gravia.repository.MavenCoordinates;
import org.jboss.gravia.repository.MavenIdentityRequirementBuilder;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryStorage;
import org.jboss.gravia.resource.DefaultRequirementBuilder;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.RequirementBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test the {@link Provisioner}.
 *
 * @author thomas.diesler@jboss.com
 * @since 06-May-2013
 */
public class ProvisionerTestCase extends AbstractProvisionerTest {

    @Test
    public void testEmptyEnvironment() {
        Environment env = getEnvironment();
        Iterator<Resource> itres = env.getResources();
        Assert.assertFalse("Empty environment", itres.hasNext());
    }

    @Test
    public void testEmptyRepository() {
        Provisioner provision = getProvisioner();
        Repository repository = provision.getRepository();
        RepositoryStorage storage = repository.adapt(RepositoryStorage.class);
        Resource resource = storage.getRepositoryReader().nextResource();
        Assert.assertNull("Empty repository", resource);
    }

    @Test
    public void testCapabilityInEnvironment() {
        ResourceBuilder cbuilder = new DefaultResourceBuilder();
        cbuilder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "res1");
        Resource res = cbuilder.getResource();
        getEnvironment().addResource(res);

        RequirementBuilder rbuilder = new DefaultRequirementBuilder(IdentityNamespace.IDENTITY_NAMESPACE, "res1");
        Requirement req = rbuilder.getRequirement();

        ProvisionResult result = findResources(Collections.singleton(req));
        Assert.assertEquals(res, result.getMapping().get(req));
        Assert.assertTrue("Empty resources", result.getResources().isEmpty());
        Assert.assertTrue("Nothing unsatisfied", result.getUnsatisfiedRequirements().isEmpty());
    }

    @Test
    public void testCapabilityInRepository() {
        ResourceBuilder cbuilder = new DefaultResourceBuilder();
        cbuilder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "res1");
        Resource res = cbuilder.getResource();
        Repository repository = getProvisioner().getRepository();
        RepositoryStorage storage = repository.adapt(RepositoryStorage.class);
        storage.addResource(res);

        RequirementBuilder rbuilder = new DefaultRequirementBuilder(IdentityNamespace.IDENTITY_NAMESPACE, "res1");
        Requirement req = rbuilder.getRequirement();

        ProvisionResult result = findResources(Collections.singleton(req));
        Assert.assertEquals(res, result.getMapping().get(req));
        Assert.assertEquals("One resource", 1, result.getResources().size());
        Assert.assertEquals(res, result.getResources().iterator().next());
        Assert.assertTrue("Nothing unsatisfied", result.getUnsatisfiedRequirements().isEmpty());
    }


    @Test
    public void testCascadingRequirement() {
        ResourceBuilder cbuilder = new DefaultResourceBuilder();
        cbuilder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "res1");
        cbuilder.addRequirement(IdentityNamespace.IDENTITY_NAMESPACE, "res2");
        Resource res1 = cbuilder.getResource();

        cbuilder = new DefaultResourceBuilder();
        cbuilder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "res2");
        Resource res2 = cbuilder.getResource();

        Repository repository = getProvisioner().getRepository();
        RepositoryStorage storage = repository.adapt(RepositoryStorage.class);
        storage.addResource(res1);
        storage.addResource(res2);

        RequirementBuilder rbuilder = new DefaultRequirementBuilder(IdentityNamespace.IDENTITY_NAMESPACE, "res1");
        Requirement req = rbuilder.getRequirement();

        ProvisionResult result = findResources(Collections.singleton(req));
        Assert.assertEquals("Two resources", 2, result.getResources().size());
        Assert.assertTrue("Nothing unsatisfied", result.getUnsatisfiedRequirements().isEmpty());
        Assert.assertEquals(res1, result.getMapping().get(req));
    }

    @Test
    public void testPreferHigherVersion() {
        ResourceBuilder cbuilder = new DefaultResourceBuilder();
        cbuilder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "res1").getAttributes().put("version", "1.0.0");
        Resource res1 = cbuilder.getResource();

        cbuilder = new DefaultResourceBuilder();
        cbuilder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "res1").getAttributes().put("version", "2.0.0");
        Resource res2 = cbuilder.getResource();

        Repository repository = getProvisioner().getRepository();
        RepositoryStorage storage = repository.adapt(RepositoryStorage.class);
        storage.addResource(res1);
        storage.addResource(res2);

        RequirementBuilder rbuilder = new DefaultRequirementBuilder(IdentityNamespace.IDENTITY_NAMESPACE, "res1");
        Requirement req = rbuilder.getRequirement();

        ProvisionResult result = findResources(Collections.singleton(req));
        Assert.assertEquals(res2, result.getMapping().get(req));
        Assert.assertEquals("One resources", 1, result.getResources().size());
        Assert.assertEquals(res2, result.getResources().iterator().next());
        Assert.assertTrue("Nothing unsatisfied", result.getUnsatisfiedRequirements().isEmpty());
    }

    @Test
    public void testMavenCoordinates() throws Exception {

        MavenCoordinates mavenid = MavenCoordinates.parse("org.jboss.spec.javax.transaction:jboss-transaction-api_1.1_spec:1.0.1.Final");
        Requirement req = new MavenIdentityRequirementBuilder(mavenid).getRequirement();

        Provisioner provisionService = getProvisioner();
        ProvisionResult result = provisionService.findResources(getEnvironment(), Collections.singleton(req));
        Assert.assertEquals("One resource", 1, result.getResources().size());
        Assert.assertTrue("Nothing unsatisfied", result.getUnsatisfiedRequirements().isEmpty());
    }
}
