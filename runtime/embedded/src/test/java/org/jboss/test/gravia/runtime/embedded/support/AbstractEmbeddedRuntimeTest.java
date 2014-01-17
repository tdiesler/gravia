/*
 * #%L
 * JBossOSGi SPI
 * %%
 * Copyright (C) 2013 JBoss by Red Hat
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
