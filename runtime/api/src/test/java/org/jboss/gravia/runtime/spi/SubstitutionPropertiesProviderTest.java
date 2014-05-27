package org.jboss.gravia.runtime.spi;


import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SubstitutionPropertiesProviderTest {

    @Test
    public void testGetProperty() throws Exception {

        Map<String, Object> config = new HashMap<>();
        config.put("key1", "value1");
        config.put("key2", "value2");
        config.put("key3", "${key2}");
        config.put("key4", "${key5}");
        config.put("key5", "${key4}");
        config.put("key6", "${key1}-${key3}");
        config.put("key7", "${key1}-${key8}");

        SubstitutionPropertiesProvider provider = new SubstitutionPropertiesProvider(new MapPropertiesProvider(config));


        org.junit.Assert.assertEquals(null, provider.getProperty(""));
        org.junit.Assert.assertEquals("value1", provider.getProperty("key1"));
        org.junit.Assert.assertEquals("value2", provider.getProperty("key3"));
        //Nested Substitution
        org.junit.Assert.assertEquals("value1-value2", provider.getProperty("key6"));
        org.junit.Assert.assertEquals("value1-", provider.getProperty("key7"));
        //Test infinite loop prevention
        org.junit.Assert.assertEquals("", provider.getProperty("key5"));
    }
}
