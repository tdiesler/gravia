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

import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.jboss.gravia.repository.Namespace100;
import org.jboss.gravia.repository.RepositoryContent;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.Version;
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

        Assert.assertEquals("One resource", 1, resources.size());
        Resource resource = resources.get(0);
        Assert.assertNotNull("Resource not null", resource);
        Assert.assertTrue(resource instanceof RepositoryContent);
        Assert.assertNull(((RepositoryContent)resource).getContent());

        ResourceIdentity icap = resource.getIdentity();
        Assert.assertEquals("acme-pool-feature", icap.getSymbolicName());
        Assert.assertEquals(Version.emptyVersion, icap.getVersion());
    }
}