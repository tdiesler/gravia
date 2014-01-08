/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2013 JBoss by Red Hat
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
package org.jboss.test.gravia.resource;


import java.util.List;

import javax.management.openmbean.CompositeData;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.ManagementResourceBuilder;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.ResourceType;
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
        Resource resout = builder.getResource();

        CompositeData resData = resout.adapt(CompositeData.class);
        String identity = (String) resData.get(ResourceType.ITEM_IDENTITY);
        Assert.assertEquals("test1:0.0.0", identity);

        Resource resin = new ManagementResourceBuilder(resData).getResource();
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
        Requirement req = builder.addRequirement(IdentityNamespace.IDENTITY_NAMESPACE, "test2");
        req.getAttributes().put("rfoo", "rbar");
        req.getDirectives().put("rone", "rtwo");
        Resource resout = builder.getResource();

        CompositeData resData = resout.adapt(CompositeData.class);
        String identity = (String) resData.get(ResourceType.ITEM_IDENTITY);
        Assert.assertEquals("test1:0.0.0", identity);

        Resource resin = new ManagementResourceBuilder(resData).getResource();
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
