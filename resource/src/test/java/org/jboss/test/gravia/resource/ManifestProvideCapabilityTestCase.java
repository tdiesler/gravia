/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.gravia.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

import junit.framework.Assert;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.gravia.resource.ManifestResourceBuilder;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.Version;
import org.junit.Test;
import org.omg.Dynamic.Parameter;

/**
 * Test the Manifest parser
 *
 * @author thomas.diesler@jboss.com
 * @since 20-Nov-2012
 */
public class ManifestProvideCapabilityTestCase {

    @Test
    public void testProvideCapability() throws Exception {
        ManifestBuilder builder = new ManifestBuilder();
        builder.addIdentityCapability("some.name", "1.0.0");
        builder.addGenericCapabilities("test; effective:=\"resolve\"; test =\"aName\"; version : Version=\"1.0\"; long :Long=\"100\"; double: Double=\"1.001\"; string:String =\"aString\"; version.list:List < Version > = \"1.0, 1.1, 1.2\"; long.list : List  <Long  >=\"1, 2, 3, 4\"; double.list: List<  Double>= \"1.001, 1.002, 1.003\"; string.list :List<String  >= \"aString,bString,cString\"; string.list2:List=\"a\\\"quote,a\\,comma, aSpace ,\\\"start,\\,start,end\\\",end\\,\"; string.list3 :List<String>= \" aString , bString , cString \"");
        builder.addGenericCapabilities("test.multiple; attr=\"value1\"", "test.multiple; attr=\"value2\"", "test.no.attrs");
        Manifest manifest = builder.getManifest();
        
        //manifest.write(System.out);
        
        ManifestResourceBuilder resbuilder = new ManifestResourceBuilder();
        Resource resource = resbuilder.load(manifest).getResource();
        Assert.assertEquals(ResourceIdentity.fromString("some.name:1.0.0"), resource.getIdentity());
        List<Capability> caps = resource.getCapabilities("test");
        Assert.assertEquals(1, caps.size());
        
        Assert.assertEquals("test", caps.get(0).getNamespace());
        Map<String, String> dirs = caps.get(0).getDirectives();
        Assert.assertEquals(1, dirs.size());
        Assert.assertEquals("resolve", dirs.get("effective"));
        Map<String, Object> atts = caps.get(0).getAttributes();
        List<String> keys = new ArrayList<String>(atts.keySet());
        Assert.assertEquals(11, keys.size());
        Assert.assertEquals("test", keys.get(0));
        Assert.assertEquals("aName", atts.get(keys.get(0)));
        Assert.assertEquals("version", keys.get(1));
        Assert.assertEquals(Version.parseVersion("1.0"), atts.get(keys.get(1)));
        Assert.assertEquals("long", keys.get(2));
        Assert.assertEquals(Long.valueOf("100"), atts.get(keys.get(2)));
        Assert.assertEquals("double", keys.get(3));
        Assert.assertEquals(Double.valueOf("1.001"), atts.get(keys.get(3)));
        Assert.assertEquals("string", keys.get(4));
        Assert.assertEquals("aString", atts.get(keys.get(4)));
        Assert.assertEquals("version.list", keys.get(5));
        List<Version> versions = Arrays.asList(Version.parseVersion("1.0"), Version.parseVersion("1.1"), Version.parseVersion("1.2"));
        Assert.assertEquals(versions, atts.get(keys.get(5)));
        Assert.assertEquals("long.list", keys.get(6));
        List<Long> longs = Arrays.asList(Long.valueOf("1"), Long.valueOf("2"), Long.valueOf("3"), Long.valueOf("4"));
        Assert.assertEquals(longs, atts.get(keys.get(6)));
        Assert.assertEquals("double.list", keys.get(7));
        List<Double> doubles = Arrays.asList(Double.valueOf("1.001"), Double.valueOf("1.002"), Double.valueOf("1.003"));
        Assert.assertEquals(doubles, atts.get(keys.get(7)));
        Assert.assertEquals("string.list", keys.get(8));
        List<String> strings = Arrays.asList("aString", "bString", "cString");
        Assert.assertEquals(strings, atts.get(keys.get(8)));
        Assert.assertEquals("string.list2", keys.get(9));
        strings = Arrays.asList("a\\\"quote", "a,comma", "aSpace", "\\\"start", ",start", "end\\\"", "end,");
        Assert.assertEquals(strings, atts.get(keys.get(9)));
        Assert.assertEquals("string.list3", keys.get(10));
        strings = Arrays.asList("aString", "bString", "cString");
        Assert.assertEquals(strings, atts.get(keys.get(10)));
        
        caps = resource.getCapabilities("test.multiple");
        Assert.assertEquals(2, caps.size());
        
        Assert.assertEquals("test.multiple", caps.get(0).getNamespace());
        atts = caps.get(0).getAttributes();
        keys = new ArrayList<String>(atts.keySet());
        Assert.assertEquals(1, keys.size());
        Assert.assertEquals("attr", keys.get(0));
        Assert.assertEquals("value1", atts.get(keys.get(0)));

        Assert.assertEquals("test.multiple", caps.get(1).getNamespace());
        atts = caps.get(1).getAttributes();
        keys = new ArrayList<String>(atts.keySet());
        Assert.assertEquals(1, keys.size());
        Assert.assertEquals("attr", keys.get(0));
        Assert.assertEquals("value2", atts.get(keys.get(0)));

        caps = resource.getCapabilities("test.no.attrs");
        Assert.assertEquals(1, caps.size());
        
        Assert.assertEquals("test.no.attrs", caps.get(0).getNamespace());
        atts = caps.get(0).getAttributes();
        Assert.assertEquals(0, atts.size());
    }
}
