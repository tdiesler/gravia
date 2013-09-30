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
package org.jboss.test.gravia.runtime.embedded;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.embedded.EmbeddedRuntime;
import org.junit.Before;

/**
 * [TODO].
 *
 * @author thomas.diesler@jbos.com
 * @since 27-Sep-2013
 */
public abstract class AbstractRuntimeTest {

    private Runtime runtime;

    @Before
    public void setUp() throws Exception {
        runtime = new EmbeddedRuntime(null);
    }

    Runtime getRuntime() {
        return runtime;
    }

    void installInternalBundles(String... names) throws Exception {
        List<Module> modules = new ArrayList<Module>();
        for (String name : names) {
            modules.add(installInternalBundle(name));
        }
        for (Module module : modules) {
            module.start();
        }
    }

    Module installInternalBundle(String symbolicName) throws Exception {
        JarFile jarFile = new JarFile("target/test-libs/bundles/" + symbolicName + ".jar");
        return runtime.installModule(getClass().getClassLoader(), jarFile.getManifest());
    }
}
