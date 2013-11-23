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

import java.util.List;
import java.util.Map;

import org.jboss.gravia.repository.Namespace100;
import org.jboss.gravia.repository.RepositoryContent;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.resource.VersionRange;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the abstract resource reader/writer
 *
 * @author thomas.diesler@jboss.com
 * @since 21-May-2012
 */
public class AbstractResourcesReaderTestCase extends AbstractRepositoryTest {

    @Test
    public void testXMLReader() throws Exception {
        RepositoryReader reader = getRepositoryReader("xml/abstract-resources.xml");
        Map<String, String> attributes = reader.getRepositoryAttributes();
        List<Resource> resources = getResources(reader);
        verifyContent(attributes, resources);
    }

    static void verifyContent(Map<String, String> attributes, List<Resource> resources) {
        Assert.assertEquals("Two attributes", 2, attributes.size());
        Assert.assertEquals("Gravia Repository", attributes.get(Namespace100.Attribute.NAME.getLocalName()));
        Assert.assertEquals("1", attributes.get(Namespace100.Attribute.INCREMENT.getLocalName()));

        Assert.assertEquals(2, resources.size());

        Resource res = resources.get(0);
        Assert.assertTrue(res instanceof RepositoryContent);
        Assert.assertNull(((RepositoryContent)res).getContent());

        ResourceIdentity resid = res.getIdentity();
        Assert.assertEquals("org.acme.foo.feature", resid.getSymbolicName());
        Assert.assertEquals(Version.emptyVersion, resid.getVersion());

        Capability icap = res.getIdentityCapability();
        Assert.assertEquals("org.acme.foo.feature", icap.getAttribute(IdentityNamespace.IDENTITY_NAMESPACE));
        Assert.assertEquals(Version.emptyVersion, icap.getAttribute(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE));

        List<Requirement> reqs = res.getRequirements(null);
        Assert.assertEquals(1, reqs.size());

        Requirement req = reqs.get(0);
        Assert.assertEquals("org.acme.foo", req.getAttribute(IdentityNamespace.IDENTITY_NAMESPACE));
        Assert.assertEquals(new VersionRange("[1.0,2.0)"), req.getAttribute(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE));

        res = resources.get(1);
        Assert.assertTrue(res instanceof RepositoryContent);
        Assert.assertNull(((RepositoryContent)res).getContent());

        resid = res.getIdentity();
        Assert.assertEquals("org.acme.foo", resid.getSymbolicName());
        Assert.assertEquals(Version.parseVersion("1.0.0"), resid.getVersion());

        icap = res.getIdentityCapability();
        Assert.assertEquals("org.acme.foo", icap.getAttribute(IdentityNamespace.IDENTITY_NAMESPACE));
        Assert.assertEquals(Version.parseVersion("1.0.0"), icap.getAttribute(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE));
        Assert.assertEquals("with,comma", icap.getAttribute("someatt"));
    }
}
