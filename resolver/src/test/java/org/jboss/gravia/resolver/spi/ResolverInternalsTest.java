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
        
        // The resolver maintains order on all levels
        // This should guarantee reproducable results
        
        Iterator<Resource> itres = wiremap.keySet().iterator();
        
        Resource res = itres.next();
        Assert.assertEquals(resE11, res);
        List<Wire> wires = wiremap.get(res);
        Assert.assertEquals(0, wires.size());
        
        res = itres.next();
        Assert.assertEquals(resB11, res);
        wires = wiremap.get(res);
        Assert.assertEquals(2, wires.size());
        Assert.assertEquals(resC10, wires.get(0).getProvider());
        Assert.assertEquals(resE11, wires.get(1).getProvider());
        
        res = itres.next();
        Assert.assertEquals(resD10, res);
        wires = wiremap.get(res);
        Assert.assertEquals(1, wires.size());
        Assert.assertEquals(resC10, wires.get(0).getProvider());

        res = itres.next();
        Assert.assertEquals(resA10, res);
        wires = wiremap.get(res);
        Assert.assertEquals(2, wires.size());
        Assert.assertEquals(resB11, wires.get(0).getProvider());
        Assert.assertEquals(resD10, wires.get(1).getProvider());
        
        Assert.assertFalse(itres.hasNext());
    }
}
