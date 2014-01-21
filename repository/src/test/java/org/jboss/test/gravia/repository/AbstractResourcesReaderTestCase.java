package org.jboss.test.gravia.repository;
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

import java.util.List;
import java.util.Map;

import org.jboss.gravia.repository.Namespace100;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceContent;
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
        Assert.assertNull(res.adapt(ResourceContent.class).getContent());

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
        Assert.assertNull(res.adapt(ResourceContent.class).getContent());

        resid = res.getIdentity();
        Assert.assertEquals("org.acme.foo", resid.getSymbolicName());
        Assert.assertEquals(Version.parseVersion("1.0.0"), resid.getVersion());

        icap = res.getIdentityCapability();
        Assert.assertEquals("org.acme.foo", icap.getAttribute(IdentityNamespace.IDENTITY_NAMESPACE));
        Assert.assertEquals(Version.parseVersion("1.0.0"), icap.getAttribute(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE));
        Assert.assertEquals("with,comma", icap.getAttribute("someatt"));
    }
}
