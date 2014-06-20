/*
 * #%L
 * Gravia :: Integration Tests :: Karaf
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
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
package org.jboss.test.gravia.itests.embedded;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test simple module lifecycle
 *
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2014
 */
@RunWith(Arquillian.class)
public class ModuleLifecycleTest {

    static AtomicInteger callCount = new AtomicInteger();

    @BeforeClass
    public static void beforeClass() {
        Assert.assertEquals(1, callCount.incrementAndGet());
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Set<Module> modules = runtime.getModules();
        Assert.assertEquals("Expected 7 modules: " + modules, 7, modules.size());
    }

    @Before
    public void setUp() {
        Assert.assertEquals(2, callCount.incrementAndGet());
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Set<Module> modules = runtime.getModules();
        Assert.assertEquals("Expected 7 modules: " + modules, 7, modules.size());
    }

    @Test
    public void testModuleLifecycle() throws Exception {
        Assert.assertEquals(3, callCount.incrementAndGet());
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Set<Module> modules = runtime.getModules();
        Assert.assertEquals("Expected 7 modules: " + modules, 7, modules.size());
    }
}
