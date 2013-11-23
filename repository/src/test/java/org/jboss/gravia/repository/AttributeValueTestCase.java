package org.jboss.gravia.repository;
/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2012 - 2013 JBoss by Red Hat
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.gravia.resource.spi.AttributeValueHandler;
import org.jboss.gravia.resource.spi.AttributeValueHandler.AttributeValue;
import org.jboss.test.gravia.repository.AbstractRepositoryTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the {@link AttributeValueHandler}.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class AttributeValueTestCase extends AbstractRepositoryTest {

    @Test
    public void testSimpleParsing() throws Exception {
        // String
        AttributeValue value = AttributeValueHandler.readAttributeValue(null, "a");
        Assert.assertEquals("a", value.getValue());
        Assert.assertEquals("a", value.getValueString());
        Assert.assertEquals(AttributeValueHandler.Type.String, value.getType());
        Assert.assertFalse(value.isListType());
        Assert.assertEquals("[type=String, value=a]", value.toExternalForm());
        Assert.assertEquals(AttributeValue.parse("[type=String, value=a]"), value);
        Assert.assertEquals(AttributeValue.create("a"), value);

        // List<String> [a, b]
        value = AttributeValueHandler.readAttributeValue("List<String>", "a,b");
        Assert.assertEquals(Arrays.asList("a","b"), value.getValue());
        Assert.assertEquals("a, b", value.getValueString());
        Assert.assertEquals(AttributeValueHandler.Type.String, value.getType());
        Assert.assertTrue(value.isListType());
        Assert.assertEquals("[type=List<String>, value=a, b]", value.toExternalForm());
        Assert.assertEquals(AttributeValue.parse("[type=List<String>, value=a, b]"), value);
        Assert.assertEquals(AttributeValue.create(Arrays.asList("a","b")), value);
    }

    @Test
    public void testCommaValueParsing() throws Exception {
        // List<String> [a,b, c,d]
        AttributeValue value = AttributeValueHandler.readAttributeValue("List<String>", "a\\,b,c\\,d");
        Assert.assertEquals(Arrays.asList("a,b","c,d"), value.getValue());
        Assert.assertEquals(value, AttributeValueHandler.readAttributeValue("List<String>", value.getValueString()));
        Assert.assertEquals(value, AttributeValue.create(Arrays.asList("a,b","c,d")));

        // List<String> [a\b, c\d]
        value = AttributeValueHandler.readAttributeValue("List<String>", "a\\b,c\\d");
        Assert.assertEquals(Arrays.asList("a\\b","c\\d"), value.getValue());
        Assert.assertEquals(value, AttributeValueHandler.readAttributeValue("List<String>", value.getValueString()));
        Assert.assertEquals(value, AttributeValue.create(Arrays.asList("a\\b","c\\d")));
    }

    @Test
    public void testLongParsing() throws Exception {
        // Long 100
        AttributeValue value = AttributeValueHandler.readAttributeValue("Long", "100");
        Assert.assertEquals(new Long(100),  value.getValue());
        Assert.assertEquals("100",  value.getValueString());

        // List<Long> [100, 200]
        value = AttributeValueHandler.readAttributeValue("List<Long>", "100, 200");
        Assert.assertEquals(Arrays.asList(new Long(100), new Long(200)), value.getValue());
        Assert.assertEquals(value, AttributeValueHandler.readAttributeValue("List<Long>", value.getValueString()));
        Assert.assertEquals(value, AttributeValue.create(Arrays.asList(new Long(100), new Long(200))));

        // List<Object> [100, 200]
        List<Object> listA = Arrays.asList((Object)new Long(100), (Object)new Long(200));
        Assert.assertEquals(value, AttributeValue.create(listA));

        // List<Long> (empty)
        value = AttributeValueHandler.readAttributeValue("List<Long>", "");
        Assert.assertEquals(Arrays.asList(), value.getValue());
        Assert.assertEquals(value, AttributeValueHandler.readAttributeValue("List<Long>", value.getValueString()));

        // Note, the component type of an empty list cannot by determined
        value = AttributeValue.create(new ArrayList<Long>());
        Assert.assertEquals(Collections.emptyList(),  value.getValue());
        Assert.assertEquals("",  value.getValueString());
        Assert.assertEquals(AttributeValueHandler.Type.String,  value.getType());
        Assert.assertTrue(value.isListType());
        Assert.assertEquals("[type=List<String>, value=]",  value.toExternalForm());
    }
}
