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
