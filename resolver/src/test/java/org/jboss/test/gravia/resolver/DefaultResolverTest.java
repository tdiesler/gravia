/*
 * #%L
 * JBossOSGi Resolver Felix
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


package org.jboss.test.gravia.resolver;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.jboss.gravia.resolver.ResolveContext;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.resource.VersionRange;
import org.jboss.gravia.resource.Wire;
import org.jboss.gravia.resource.Wiring;
import org.junit.Test;


/**
 * Test the default resolver integration.
 * 
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public class DefaultResolverTest extends AbstractResolverTest {
    
    @Test
    public void testSingleDependency() throws Exception {
        
        ResourceBuilder builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resA", new Version("1.0.0"));
        Requirement reqA = builder.addIdentityRequirement("resB", new VersionRange("[1.0,2.0)"));
        Resource resA = builder.getResource();
        
        builder = new DefaultResourceBuilder();
        Capability capB = builder.addIdentityCapability("resB", new Version("1.0.0"));
        Resource resB = builder.getResource();
        
        installResources(resA, resB);
        
        ResolveContext context = getResolveContext(Arrays.asList(resA, resB), null);
        Map<Resource, List<Wire>> wiremap = resolveAndApply(context);
        Assert.assertEquals(2, wiremap.size());
        List<Wire> wiresA = wiremap.get(resA);
        Assert.assertEquals(1, wiresA.size());
        Wire wireA = wiresA.get(0);
        Assert.assertEquals(resA, wireA.getRequirer());
        Assert.assertEquals(reqA, wireA.getRequirement());
        Assert.assertEquals(resB, wireA.getProvider());
        Assert.assertEquals(capB, wireA.getCapability());
        
        Wiring wiringA = resA.getWiring();
        Assert.assertEquals(resA, wiringA.getResource());
        Assert.assertEquals(0, wiringA.getProvidedResourceWires(null).size());
        Assert.assertEquals(1, wiringA.getRequiredResourceWires(null).size());
        Assert.assertEquals(wireA, wiringA.getRequiredResourceWires(null).get(0));
        Assert.assertEquals(0, wiringA.getResourceCapabilities(null).size());
        Assert.assertEquals(1, wiringA.getResourceRequirements(null).size());
        Assert.assertEquals(reqA, wiringA.getResourceRequirements(null).get(0));
        
        Wiring wiringB = resB.getWiring();
        Assert.assertEquals(resB, wiringB.getResource());
        Assert.assertEquals(1, wiringB.getProvidedResourceWires(null).size());
        Assert.assertEquals(0, wiringB.getRequiredResourceWires(null).size());
        Assert.assertEquals(wireA, wiringB.getProvidedResourceWires(null).get(0));
        Assert.assertEquals(1, wiringB.getResourceCapabilities(null).size());
        Assert.assertEquals(0, wiringB.getResourceRequirements(null).size());
        Assert.assertEquals(capB, wiringB.getResourceCapabilities(null).get(0));
    }

}
