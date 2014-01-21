/*
 * #%L
 * Gravia :: Runtime :: Embedded
 * %%
 * Copyright (C) 2013 - 2014 JBoss by Red Hat
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
package org.jboss.test.gravia.runtime.embedded.support;

import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.embedded.internal.EmbeddedRuntime;
import org.jboss.gravia.runtime.spi.ModuleEntriesProvider;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.spi.RuntimeFactory;
import org.jboss.gravia.runtime.util.ClassLoaderEntriesProvider;
import org.jboss.gravia.runtime.util.DefaultPropertiesProvider;
import org.junit.After;
import org.junit.Before;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Abstract embedded runtome test.
 *
 * @author thomas.diesler@jbos.com
 * @since 27-Sep-2013
 */
public abstract class AbstractEmbeddedRuntimeTest {

    private Runtime runtime;

    @Before
    public void setUp() throws Exception {
        PropertiesProvider propsProvider = new DefaultPropertiesProvider();
        RuntimeFactory factory = new RuntimeFactory() {
            @Override
            public Runtime createRuntime(PropertiesProvider propertiesProvider) {
                return new EmbeddedRuntime(propertiesProvider, null) {
                    @Override
                    protected ModuleEntriesProvider getDefaultEntriesProvider(Module module, Attachable context) {
                        return new ClassLoaderEntriesProvider(module);
                    }
                };
            }
        };
        runtime = RuntimeLocator.createRuntime(factory, propsProvider);
        runtime.init();
    }

    @After
    public void tearDown() throws Exception {
        RuntimeLocator.releaseRuntime();
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public ConfigurationAdmin getConfigurationAdmin(Module module) {
        ModuleContext context = module.getModuleContext();
        return context.getService(context.getServiceReference(ConfigurationAdmin.class));
    }
}
