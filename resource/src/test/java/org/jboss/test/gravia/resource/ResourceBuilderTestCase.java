/*
 * #%L
 * JBossOSGi Resolver API
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
package org.jboss.test.gravia.resource;


import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.resource.spi.AbstractResourceBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the {@link AbstractResourceBuilder} class
 *
 * @author Thomas.Diesler@jboss.com
 */
public class ResourceBuilderTestCase  {

    @Test
    public void testAttributeMutability() throws Exception {
        ResourceBuilder builder = new DefaultResourceBuilder();
        Capability cap = builder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "test1");
        cap.getAttributes().put("foo", "bar");
        Resource res = builder.getResource();
        ResourceIdentity resid = res.getIdentity();
        Assert.assertEquals("test1", resid.getSymbolicName());
        Assert.assertEquals(Version.emptyVersion, resid.getVersion());
        Capability icap = res.getIdentityCapability();
        Assert.assertEquals("bar", icap.getAttribute("foo"));
        try {
            icap.getAttributes().remove("foo");
            Assert.fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }
}
