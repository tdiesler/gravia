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


package org.jboss.gravia.resolver.spi;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.jboss.gravia.resolver.ResolveContext;
import org.jboss.gravia.resolver.spi.AbstractResolver.ResourceCandidates;
import org.jboss.gravia.resolver.spi.AbstractResolver.ResourceSpace;
import org.jboss.gravia.resolver.spi.AbstractResolver.ResourceSpaces;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.Wire;
import org.junit.Test;


/**
 * Test the default resolver integration.
 * 
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public class ResolverInternalsTest extends AbstractResolverInternalsTest {
    
    @Test
    public void testInitialResourceSpaces() throws Exception {
        Resource resC10 = store.getResource(ResourceIdentity.fromString("resC:1.0.0"));
        
        ResolveContext context = getResolveContext(null, null);
        ResourceSpaces spaces = resolver.createResourceSpaces(context);
        Map<Resource, ResourceSpace> spacemap = spaces.getResourceSpaces();
        Assert.assertEquals("One wired space", 1, spacemap.size());
        ResourceSpace space = spacemap.values().iterator().next();
        Assert.assertEquals(resC10, space.getPrimary());
        Assert.assertEquals(1, space.getResources().size());
        Assert.assertEquals(resC10, space.getResources().iterator().next());
    }

    @Test
    public void testResourceCandidates() throws Exception {
        Resource resA10 = store.getResource(ResourceIdentity.fromString("resA:1.0.0"));
        Resource resB10 = store.getResource(ResourceIdentity.fromString("resB:1.0.0"));
        Resource resB11 = store.getResource(ResourceIdentity.fromString("resB:1.1.0"));
        Resource resC10 = store.getResource(ResourceIdentity.fromString("resC:1.0.0"));
        Resource resC11 = store.getResource(ResourceIdentity.fromString("resC:1.1.0"));
        Resource resD10 = store.getResource(ResourceIdentity.fromString("resD:1.0.0"));
        Resource resE10 = store.getResource(ResourceIdentity.fromString("resE:1.0.0"));
        Resource resE11 = store.getResource(ResourceIdentity.fromString("resE:1.1.0"));
        
        ResolveContext context = getResolveContext(null, null);
        
        // A-1.0.0
        ResourceCandidates rescan = resolver.createResourceCandidates(resA10);
        Iterator<List<Wire>> itcan = rescan.iterator(context);
        Assert.assertTrue(itcan.hasNext());
        List<Wire> wires = itcan.next();
        Assert.assertEquals(2, wires.size());
        Assert.assertEquals(resB11, wires.get(0).getProvider());
        Assert.assertEquals(resD10, wires.get(1).getProvider());
        
        Assert.assertTrue(itcan.hasNext());
        wires = itcan.next();
        Assert.assertEquals(2, wires.size());
        Assert.assertEquals(resB10, wires.get(0).getProvider());
        Assert.assertEquals(resD10, wires.get(1).getProvider());
        Assert.assertFalse(itcan.hasNext());
        
        // B-1.0.0
        rescan = resolver.createResourceCandidates(resB10);
        itcan = rescan.iterator(context);
        Assert.assertTrue(itcan.hasNext());
        wires = itcan.next();
        Assert.assertEquals(2, wires.size());
        Assert.assertEquals(resC10, wires.get(0).getProvider());
        Assert.assertEquals(resE11, wires.get(1).getProvider());
        
        Assert.assertTrue(itcan.hasNext());
        wires = itcan.next();
        Assert.assertEquals(2, wires.size());
        Assert.assertEquals(resC10, wires.get(0).getProvider());
        Assert.assertEquals(resE10, wires.get(1).getProvider());
        
        Assert.assertTrue(itcan.hasNext());
        wires = itcan.next();
        Assert.assertEquals(2, wires.size());
        Assert.assertEquals(resC11, wires.get(0).getProvider());
        Assert.assertEquals(resE11, wires.get(1).getProvider());
        
        Assert.assertTrue(itcan.hasNext());
        wires = itcan.next();
        Assert.assertEquals(2, wires.size());
        Assert.assertEquals(resC11, wires.get(0).getProvider());
        Assert.assertEquals(resE10, wires.get(1).getProvider());
        Assert.assertFalse(itcan.hasNext());
        
        // B-1.1.0
        rescan = resolver.createResourceCandidates(resB11);
        itcan = rescan.iterator(context);
        Assert.assertTrue(itcan.hasNext());
        wires = itcan.next();
        Assert.assertEquals(2, wires.size());
        Assert.assertEquals(resC10, wires.get(0).getProvider());
        Assert.assertEquals(resE11, wires.get(1).getProvider());
        
        Assert.assertTrue(itcan.hasNext());
        wires = itcan.next();
        Assert.assertEquals(2, wires.size());
        Assert.assertEquals(resC10, wires.get(0).getProvider());
        Assert.assertEquals(resE10, wires.get(1).getProvider());
        
        Assert.assertTrue(itcan.hasNext());
        wires = itcan.next();
        Assert.assertEquals(2, wires.size());
        Assert.assertEquals(resC11, wires.get(0).getProvider());
        Assert.assertEquals(resE11, wires.get(1).getProvider());
        
        Assert.assertTrue(itcan.hasNext());
        wires = itcan.next();
        Assert.assertEquals(2, wires.size());
        Assert.assertEquals(resC11, wires.get(0).getProvider());
        Assert.assertEquals(resE10, wires.get(1).getProvider());
        Assert.assertFalse(itcan.hasNext());
        
        // C-1.0.0
        rescan = resolver.createResourceCandidates(resC10);
        itcan = rescan.iterator(context);
        Assert.assertTrue(itcan.hasNext());
        wires = itcan.next();
        Assert.assertEquals(0, wires.size());
        Assert.assertFalse(itcan.hasNext());
        
        // C-1.1.0
        rescan = resolver.createResourceCandidates(resC11);
        itcan = rescan.iterator(context);
        Assert.assertTrue(itcan.hasNext());
        wires = itcan.next();
        Assert.assertEquals(0, wires.size());
        Assert.assertFalse(itcan.hasNext());
        
        // D-1.0.0
        rescan = resolver.createResourceCandidates(resD10);
        itcan = rescan.iterator(context);
        Assert.assertTrue(itcan.hasNext());
        wires = itcan.next();
        Assert.assertEquals(1, wires.size());
        Assert.assertEquals(resC10, wires.get(0).getProvider());
        
        Assert.assertTrue(itcan.hasNext());
        wires = itcan.next();
        Assert.assertEquals(1, wires.size());
        Assert.assertEquals(resC11, wires.get(0).getProvider());
        Assert.assertFalse(itcan.hasNext());
        
        // E-1.0.0
        rescan = resolver.createResourceCandidates(resE10);
        itcan = rescan.iterator(context);
        Assert.assertTrue(itcan.hasNext());
        wires = itcan.next();
        Assert.assertEquals(0, wires.size());
        Assert.assertFalse(itcan.hasNext());
        
        // E-1.1.0
        rescan = resolver.createResourceCandidates(resE11);
        itcan = rescan.iterator(context);
        Assert.assertTrue(itcan.hasNext());
        wires = itcan.next();
        Assert.assertEquals(0, wires.size());
        Assert.assertFalse(itcan.hasNext());
    }

    @Test
    public void testResolveResources() throws Exception {
        Resource resA10 = store.getResource(ResourceIdentity.fromString("resA:1.0.0"));
        Resource resB11 = store.getResource(ResourceIdentity.fromString("resB:1.1.0"));
        Resource resC10 = store.getResource(ResourceIdentity.fromString("resC:1.0.0"));
        Resource resD10 = store.getResource(ResourceIdentity.fromString("resD:1.0.0"));
        Resource resE11 = store.getResource(ResourceIdentity.fromString("resE:1.1.0"));
        
        ResolveContext context = getResolveContext(Arrays.asList(resA10), null);
        Map<Resource, List<Wire>> wiremap = resolver.resolveAndApply(context);
        Assert.assertEquals(4, wiremap.size());
        
        List<Wire> wires = wiremap.get(resA10);
        Assert.assertEquals(2, wires.size());
        Assert.assertEquals(resB11, wires.get(0).getProvider());
        Assert.assertEquals(resD10, wires.get(1).getProvider());
        
        wires = wiremap.get(resB11);
        Assert.assertEquals(2, wires.size());
        Assert.assertEquals(resC10, wires.get(0).getProvider());
        Assert.assertEquals(resE11, wires.get(1).getProvider());
        
        wires = wiremap.get(resD10);
        Assert.assertEquals(1, wires.size());
        Assert.assertEquals(resC10, wires.get(0).getProvider());
        
        wires = wiremap.get(resE11);
        Assert.assertEquals(0, wires.size());
    }
}
