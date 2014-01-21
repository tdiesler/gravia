/*
 * #%L
 * Gravia :: Resource
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
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


import java.util.List;

import javax.management.openmbean.CompositeData;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.CompositeDataResourceBuilder;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.CompositeDataResourceType;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link Resource} openmbean type serialization
 *
 * @author Thomas.Diesler@jboss.com
 */
public class ResourceTypeTestCase  {

    @Test
    public void testCompositeData() throws Exception {
        ResourceBuilder builder = new DefaultResourceBuilder();
        Capability cap = builder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "test1");
        cap.getAttributes().put("cfoo", "cbar");
        cap.getDirectives().put("cone", "ctwo");
        builder.addCapability("some.namespace", "some.value");
        Resource resout = builder.getResource();

        CompositeData resData = resout.adapt(CompositeData.class);
        String identity = (String) resData.get(CompositeDataResourceType.ITEM_IDENTITY);
        Assert.assertEquals("test1:0.0.0", identity);

        Resource resin = new CompositeDataResourceBuilder(resData).getResource();
        ResourceIdentity resid = resin.getIdentity();
        Assert.assertEquals("test1:0.0.0", resid.toString());
        Capability icap = resin.getIdentityCapability();
        Assert.assertEquals("cbar", icap.getAttribute("cfoo"));
        Assert.assertEquals("ctwo", icap.getDirective("cone"));
        List<Requirement> reqs = resin.getRequirements(null);
        Assert.assertEquals(0, reqs.size());
    }

    @Test
    public void testCompositeDataWithRequirements() throws Exception {
        ResourceBuilder builder = new DefaultResourceBuilder();
        Capability cap = builder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "test1");
        cap.getAttributes().put("cfoo", "cbar");
        cap.getDirectives().put("cone", "ctwo");
        builder.addCapability("some.namespace", "some.value");
        Requirement req = builder.addRequirement(IdentityNamespace.IDENTITY_NAMESPACE, "test2");
        req.getAttributes().put("rfoo", "rbar");
        req.getDirectives().put("rone", "rtwo");
        Resource resout = builder.getResource();

        CompositeData resData = resout.adapt(CompositeData.class);
        String identity = (String) resData.get(CompositeDataResourceType.ITEM_IDENTITY);
        Assert.assertEquals("test1:0.0.0", identity);

        Resource resin = new CompositeDataResourceBuilder(resData).getResource();
        ResourceIdentity resid = resin.getIdentity();
        Assert.assertEquals("test1:0.0.0", resid.toString());
        Capability icap = resin.getIdentityCapability();
        Assert.assertEquals("cbar", icap.getAttribute("cfoo"));
        Assert.assertEquals("ctwo", icap.getDirective("cone"));
        List<Requirement> reqs = resin.getRequirements(null);
        Assert.assertEquals(1, reqs.size());
        Requirement ireq = reqs.get(0);
        Assert.assertEquals("rbar", ireq.getAttribute("rfoo"));
        Assert.assertEquals("rtwo", ireq.getDirective("rone"));
    }
}
