/*
 * #%L
 * JBossOSGi Resolver Felix
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


package org.jboss.test.gravia.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.jboss.gravia.resolver.ResolutionException;
import org.jboss.gravia.resolver.ResolveContext;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.Namespace;
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
        
        Iterator<Resource> itres = wiremap.keySet().iterator();
        
        Resource res = itres.next();
        Assert.assertEquals(resB, res);
        List<Wire> wiresB = wiremap.get(res);
        Assert.assertEquals(0, wiresB.size());
        
        res = itres.next();
        Assert.assertEquals(resA, res);
        List<Wire> wiresA = wiremap.get(res);
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
        
        // Resolve again
        context = getResolveContext(Arrays.asList(resA, resB), null);
        wiremap = resolveAndApply(context);
        Assert.assertEquals(0, wiremap.size());
    }

    @Test
    public void testSingleDependencyResolveOptional() throws Exception {
        
        ResourceBuilder builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resA", new Version("1.0.0"));
        Requirement reqA = builder.addIdentityRequirement("resB", new VersionRange("[1.0,2.0)"));
        Resource resA = builder.getResource();
        
        builder = new DefaultResourceBuilder();
        Capability capB = builder.addIdentityCapability("resB", new Version("1.0.0"));
        Resource resB = builder.getResource();
        
        installResources(resA, resB);
        
        ResolveContext context = getResolveContext(null, Arrays.asList(resA, resB));
        Map<Resource, List<Wire>> wiremap = resolveAndApply(context);
        Assert.assertEquals(2, wiremap.size());
        
        Iterator<Resource> itres = wiremap.keySet().iterator();
        
        Resource res = itres.next();
        Assert.assertEquals(resB, res);
        List<Wire> wiresB = wiremap.get(res);
        Assert.assertEquals(0, wiresB.size());
        
        res = itres.next();
        Assert.assertEquals(resA, res);
        List<Wire> wiresA = wiremap.get(res);
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

    @Test
    public void testAgainstAleadyWired() throws Exception {
        
        ResourceBuilder builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resA", new Version("1.0.0"));
        Requirement reqA = builder.addIdentityRequirement("resB", new VersionRange("[1.0,2.0)"));
        Resource resA = builder.getResource();
        
        builder = new DefaultResourceBuilder();
        Capability capB = builder.addIdentityCapability("resB", new Version("1.0.0"));
        Resource resB = builder.getResource();
        
        installResources(resB);
        
        ResolveContext context = getResolveContext(Arrays.asList(resB), null);
        Map<Resource, List<Wire>> wiremap = resolveAndApply(context);
        Assert.assertEquals(1, wiremap.size());
        
        List<Wire> wiresB = wiremap.get(resB);
        Assert.assertEquals(0, wiresB.size());
        
        Wiring wiringB = resB.getWiring();
        Assert.assertEquals(resB, wiringB.getResource());
        Assert.assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        Assert.assertEquals(0, wiringB.getRequiredResourceWires(null).size());
        Assert.assertEquals(0, wiringB.getResourceCapabilities(null).size());
        Assert.assertEquals(0, wiringB.getResourceRequirements(null).size());

        installResources(resA);
        
        context = getResolveContext(Arrays.asList(resA), null);
        wiremap = resolveAndApply(context);
        Assert.assertEquals(1, wiremap.size());

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
        
        Assert.assertSame(wiringB, resB.getWiring());
        Assert.assertEquals(resB, wiringB.getResource());
        Assert.assertEquals(1, wiringB.getProvidedResourceWires(null).size());
        Assert.assertEquals(0, wiringB.getRequiredResourceWires(null).size());
        Assert.assertEquals(wireA, wiringB.getProvidedResourceWires(null).get(0));
        Assert.assertEquals(1, wiringB.getResourceCapabilities(null).size());
        Assert.assertEquals(0, wiringB.getResourceRequirements(null).size());
        Assert.assertEquals(capB, wiringB.getResourceCapabilities(null).get(0));
    }

    @Test
    public void testMissingDependency() throws Exception {
        
        ResourceBuilder builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resA", new Version("1.0.0"));
        Requirement reqA = builder.addIdentityRequirement("resB", new VersionRange("[1.0,2.0)"));
        Resource resA = builder.getResource();
        
        installResources(resA);
        
        ResolveContext context = getResolveContext(Arrays.asList(resA), null);
        try {
            resolveAndApply(context);
            Assert.fail("ResolutionException expected");
        } catch (ResolutionException ex) {
            List<Requirement> unresolved = new ArrayList<Requirement>(ex.getUnresolvedRequirements());
            Assert.assertEquals(1, unresolved.size());
            Assert.assertEquals(reqA, unresolved.get(0));
        }
    }

    @Test
    public void testMissingDependencyResolveOptional() throws Exception {
        
        ResourceBuilder builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resA", new Version("1.0.0"));
        builder.addIdentityRequirement("resB", new VersionRange("[1.0,2.0)"));
        Resource resA = builder.getResource();
        
        installResources(resA);
        
        ResolveContext context = getResolveContext(null, Arrays.asList(resA));
        Map<Resource, List<Wire>> wiremap = resolveAndApply(context);
        Assert.assertEquals(1, wiremap.size());
        
        List<Wire> wiresA = wiremap.get(resA);
        Assert.assertEquals(0, wiresA.size());
    }

    @Test
    public void testMissingOptionalDependency() throws Exception {
        
        ResourceBuilder builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resA", new Version("1.0.0"));
        Requirement reqA = builder.addIdentityRequirement("resB", new VersionRange("[1.0,2.0)"));
        reqA.getDirectives().put(Namespace.REQUIREMENT_RESOLUTION_DIRECTIVE, Namespace.RESOLUTION_OPTIONAL);
        Resource resA = builder.getResource();
        
        installResources(resA);
        
        ResolveContext context = getResolveContext(Arrays.asList(resA), null);
        Map<Resource, List<Wire>> wiremap = resolveAndApply(context);
        Assert.assertEquals(1, wiremap.size());
        
        List<Wire> wiresA = wiremap.get(resA);
        Assert.assertEquals(0, wiresA.size());

        Wiring wiringA = resA.getWiring();
        Assert.assertEquals(resA, wiringA.getResource());
        Assert.assertEquals(0, wiringA.getProvidedResourceWires(null).size());
        Assert.assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        Assert.assertEquals(0, wiringA.getResourceCapabilities(null).size());
        Assert.assertEquals(0, wiringA.getResourceRequirements(null).size());
    }

    @Test
    public void testPreferHigherVersion() throws Exception {
        
        ResourceBuilder builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resA", new Version("1.0.0"));
        Requirement reqA = builder.addIdentityRequirement("resB", new VersionRange("[1.0,2.0)"));
        Resource resA = builder.getResource();
        
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resB", new Version("1.0.0"));
        Resource resB1 = builder.getResource();
        
        builder = new DefaultResourceBuilder();
        Capability capB2 = builder.addIdentityCapability("resB", new Version("1.1.0"));
        Resource resB2 = builder.getResource();
        
        installResources(resA, resB1, resB2);
        
        ResolveContext context = getResolveContext(Arrays.asList(resA), null);
        Map<Resource, List<Wire>> wiremap = resolveAndApply(context);
        Assert.assertEquals(2, wiremap.size());
        
        List<Wire> wiresA = wiremap.get(resA);
        Assert.assertEquals(1, wiresA.size());
        Wire wireA = wiresA.get(0);
        Assert.assertEquals(resA, wireA.getRequirer());
        Assert.assertEquals(reqA, wireA.getRequirement());
        Assert.assertEquals(resB2, wireA.getProvider());
        Assert.assertEquals(capB2, wireA.getCapability());
        
        List<Wire> wiresB2 = wiremap.get(resB2);
        Assert.assertEquals(0, wiresB2.size());
        
        Wiring wiringA = resA.getWiring();
        Assert.assertEquals(resA, wiringA.getResource());
        Assert.assertEquals(0, wiringA.getProvidedResourceWires(null).size());
        Assert.assertEquals(1, wiringA.getRequiredResourceWires(null).size());
        Assert.assertEquals(wireA, wiringA.getRequiredResourceWires(null).get(0));
        Assert.assertEquals(0, wiringA.getResourceCapabilities(null).size());
        Assert.assertEquals(1, wiringA.getResourceRequirements(null).size());
        Assert.assertEquals(reqA, wiringA.getResourceRequirements(null).get(0));
        
        Wiring wiringB1 = resB1.getWiring();
        Assert.assertNull(wiringB1);

        Wiring wiringB2 = resB2.getWiring();
        Assert.assertEquals(resB2, wiringB2.getResource());
        Assert.assertEquals(1, wiringB2.getProvidedResourceWires(null).size());
        Assert.assertEquals(0, wiringB2.getRequiredResourceWires(null).size());
        Assert.assertEquals(wireA, wiringB2.getProvidedResourceWires(null).get(0));
        Assert.assertEquals(1, wiringB2.getResourceCapabilities(null).size());
        Assert.assertEquals(0, wiringB2.getResourceRequirements(null).size());
        Assert.assertEquals(capB2, wiringB2.getResourceCapabilities(null).get(0));
    }

    @Test
    public void testPreferAlreadyWired() throws Exception {
        
        ResourceBuilder builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resA", new Version("1.0.0"));
        Requirement reqA = builder.addIdentityRequirement("resB", new VersionRange("[1.0,2.0)"));
        Resource resA = builder.getResource();
        
        builder = new DefaultResourceBuilder();
        Capability capB1 = builder.addIdentityCapability("resB", new Version("1.0.0"));
        Resource resB1 = builder.getResource();
        
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resB", new Version("1.1.0"));
        Resource resB2 = builder.getResource();
        
        installResources(resB1);
        
        ResolveContext context = getResolveContext(Arrays.asList(resB1), null);
        Map<Resource, List<Wire>> wiremap = resolveAndApply(context);
        Assert.assertEquals(1, wiremap.size());
        
        installResources(resA, resB2);
        
        context = getResolveContext(Arrays.asList(resA), null);
        wiremap = resolveAndApply(context);
        Assert.assertEquals(1, wiremap.size());
        
        List<Wire> wiresA = wiremap.get(resA);
        Wire wireA = wiresA.get(0);
        Assert.assertEquals(resA, wireA.getRequirer());
        Assert.assertEquals(reqA, wireA.getRequirement());
        Assert.assertEquals(resB1, wireA.getProvider());
        Assert.assertEquals(capB1, wireA.getCapability());
        
        Wiring wiringA = resA.getWiring();
        Assert.assertEquals(resA, wiringA.getResource());
        Assert.assertEquals(0, wiringA.getProvidedResourceWires(null).size());
        Assert.assertEquals(1, wiringA.getRequiredResourceWires(null).size());
        Assert.assertEquals(wireA, wiringA.getRequiredResourceWires(null).get(0));
        Assert.assertEquals(0, wiringA.getResourceCapabilities(null).size());
        Assert.assertEquals(1, wiringA.getResourceRequirements(null).size());
        Assert.assertEquals(reqA, wiringA.getResourceRequirements(null).get(0));
    }

    @Test
    public void testCascadingResolve() throws Exception {
        
        ResourceBuilder builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resA", new Version("1.0.0"));
        Requirement reqA = builder.addIdentityRequirement("resB", new VersionRange("[1.0,2.0)"));
        Resource resA = builder.getResource();
        
        builder = new DefaultResourceBuilder();
        Capability capB = builder.addIdentityCapability("resB", new Version("1.0.0"));
        Requirement reqB = builder.addIdentityRequirement("resC", new VersionRange("[1.0,2.0)"));
        Resource resB = builder.getResource();
        
        builder = new DefaultResourceBuilder();
        Capability capC = builder.addIdentityCapability("resC", new Version("1.0.0"));
        Resource resC = builder.getResource();
        
        installResources(resA, resB, resC);
        
        ResolveContext context = getResolveContext(Arrays.asList(resA), null);
        Map<Resource, List<Wire>> wiremap = resolveAndApply(context);
        Assert.assertEquals(3, wiremap.size());
        
        Iterator<Resource> itres = wiremap.keySet().iterator();
        
        Resource res = itres.next();
        Assert.assertEquals(resC, res);
        List<Wire> wiresC = wiremap.get(res);
        Assert.assertEquals(0, wiresC.size());
        
        res = itres.next();
        Assert.assertEquals(resB, res);
        List<Wire> wiresB = wiremap.get(res);
        Assert.assertEquals(1, wiresB.size());
        Wire wireB = wiresB.get(0);
        Assert.assertEquals(resB, wireB.getRequirer());
        Assert.assertEquals(reqB, wireB.getRequirement());
        Assert.assertEquals(resC, wireB.getProvider());
        Assert.assertEquals(capC, wireB.getCapability());
        
        res = itres.next();
        Assert.assertEquals(resA, res);
        List<Wire> wiresA = wiremap.get(res);
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
        Assert.assertEquals(1, wiringB.getRequiredResourceWires(null).size());
        Assert.assertEquals(wireA, wiringB.getProvidedResourceWires(null).get(0));
        Assert.assertEquals(wireB, wiringB.getRequiredResourceWires(null).get(0));
        Assert.assertEquals(1, wiringB.getResourceCapabilities(null).size());
        Assert.assertEquals(1, wiringB.getResourceRequirements(null).size());
        Assert.assertEquals(capB, wiringB.getResourceCapabilities(null).get(0));
        Assert.assertEquals(reqB, wiringB.getResourceRequirements(null).get(0));

        Wiring wiringC = resC.getWiring();
        Assert.assertEquals(resC, wiringC.getResource());
        Assert.assertEquals(1, wiringC.getProvidedResourceWires(null).size());
        Assert.assertEquals(0, wiringC.getRequiredResourceWires(null).size());
        Assert.assertEquals(wireB, wiringC.getProvidedResourceWires(null).get(0));
        Assert.assertEquals(1, wiringC.getResourceCapabilities(null).size());
        Assert.assertEquals(0, wiringC.getResourceRequirements(null).size());
        Assert.assertEquals(capC, wiringC.getResourceCapabilities(null).get(0));
    }

    @Test
    public void testInconsistentClassSpace() throws Exception {
        
        // A-1.0.0 => B-1.0.0
        
        ResourceBuilder builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resA", new Version("1.0.0"));
        builder.addIdentityRequirement("resB", new VersionRange("[1.0,2.0)"));
        Resource resA = builder.getResource();
        
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resB", new Version("1.0.0"));
        Resource resB1 = builder.getResource();
        
        installResources(resA, resB1);
        
        ResolveContext context = getResolveContext(Arrays.asList(resA), null);
        Map<Resource, List<Wire>> wiremap = resolveAndApply(context);
        Assert.assertEquals(2, wiremap.size());

        // C-1.0.0 => A-1.0.0,B-1.1.0
        
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resB", new Version("1.1.0"));
        Resource resB2 = builder.getResource();
        
        builder = new DefaultResourceBuilder();
        builder.addIdentityCapability("resC", new Version("1.0.0"));
        builder.addIdentityRequirement("resA", new VersionRange("[1.0,2.0)"));
        Requirement reqC2 = builder.addIdentityRequirement("resB", new VersionRange("[1.1,2.0)"));
        Resource resC = builder.getResource();
        
        installResources(resC, resB2);
        
        context = getResolveContext(Arrays.asList(resC), null);
        try {
            resolveAndApply(context);
            Assert.fail("ResolutionException expected");
        } catch (ResolutionException ex) {
            Collection<Requirement> unresolved = ex.getUnresolvedRequirements();
            Assert.assertTrue(unresolved.contains(reqC2));
        }
    }
}
