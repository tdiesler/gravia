package org.jboss.gravia.runtime.spi;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


public class SubstitutionPropertiesProviderTest {

    Map<String, Object> config = new HashMap<>();

    @Before
    public void setUp() {
        config.put("key1", "value1");
        config.put("key2", "value2");
        config.put("key3", "${key2}");
        config.put("key4", "${key5}");
        config.put("key5", "${key4}");
        config.put("key6", "${key1}-${key3}");
        config.put("key7", "${key1}-${key8}");

        //Let's add a cycle
        config.put("env1", "${env1}");
        config.put("env2", "${env2}");

    }

    @Test
    public void testGetProperty() throws Exception {
        SubstitutionPropertiesProvider provider = new SubstitutionPropertiesProvider(new MapPropertiesProvider(config));
        Assert.assertEquals(null, provider.getProperty(""));
        Assert.assertEquals("value1", provider.getProperty("key1"));
        Assert.assertEquals("value2", provider.getProperty("key3"));
        //Nested Substitution
        Assert.assertEquals("value1-value2", provider.getProperty("key6"));
        Assert.assertEquals("value1-", provider.getProperty("key7"));
        //Test infinite loop prevention
        Assert.assertEquals(null, provider.getProperty("key5"));
        Assert.assertEquals(null, provider.getProperty("env1"));
        Assert.assertEquals(null, provider.getProperty("env2"));

    }

    @Test
    public void testWithSystemProperties() throws Exception {
        SubstitutionPropertiesProvider provider = new SubstitutionPropertiesProvider(new MapPropertiesProvider(config), new SystemPropertiesProvider());
        Assert.assertEquals(null, provider.getProperty(""));
        Assert.assertEquals("value1", provider.getProperty("key1"));
        Assert.assertEquals("value2", provider.getProperty("key3"));
        //Nested Substitution
        Assert.assertEquals("value1-value2", provider.getProperty("key6"));
        Assert.assertEquals("value1-", provider.getProperty("key7"));
        //Test infinite loop prevention
        Assert.assertEquals(null, provider.getProperty("key5"));
        Assert.assertEquals("system.value1", provider.getProperty("env1"));
        Assert.assertEquals(null, provider.getProperty("env2"));
    }

    @Test
    public void testWithSystemAndEnvProperties() throws Exception {
        SubstitutionPropertiesProvider provider = new SubstitutionPropertiesProvider(new MapPropertiesProvider(config), new SystemPropertiesProvider(), new EnvPropertiesProvider());
        Assert.assertEquals(null, provider.getProperty(""));
        Assert.assertEquals("value1", provider.getProperty("key1"));
        Assert.assertEquals("value2", provider.getProperty("key3"));
        //Nested Substitution
        Assert.assertEquals("value1-value2", provider.getProperty("key6"));
        Assert.assertEquals("value1-", provider.getProperty("key7"));
        //Test infinite loop prevention
        Assert.assertEquals(null, provider.getProperty("key5"));
        Assert.assertEquals("system.value1", provider.getProperty("env1"));
        Assert.assertEquals("gravia.value2", provider.getProperty("env2"));
    }

    @Test
    public void testWithSystemAndEnvWithCustomPrefixProperties() throws Exception {
        Map<String, Object> envConf = new HashMap<>();
        envConf.put(EnvPropertiesProvider.ENV_PREFIX_KEY, "CUSTOM_");
        SubstitutionPropertiesProvider provider = new SubstitutionPropertiesProvider(new MapPropertiesProvider(config), new SystemPropertiesProvider(), new EnvPropertiesProvider(new MapPropertiesProvider(envConf)));
        Assert.assertEquals(null, provider.getProperty(""));
        Assert.assertEquals("value1", provider.getProperty("key1"));
        Assert.assertEquals("value2", provider.getProperty("key3"));
        //Nested Substitution
        Assert.assertEquals("value1-value2", provider.getProperty("key6"));
        Assert.assertEquals("value1-", provider.getProperty("key7"));
        //Test infinite loop prevention
        Assert.assertEquals(null, provider.getProperty("key5"));
        Assert.assertEquals("system.value1", provider.getProperty("env1"));
        Assert.assertEquals("custom.value2", provider.getProperty("env2"));
    }
}
