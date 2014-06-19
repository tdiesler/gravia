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
package org.jboss.gravia.arquillian.container.embedded;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.gravia.arquillian.container.SetupTask;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.junit.Assert;

/**
 * Test simple embedded test setup
 *
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2014
 */
public class EmbeddedSetupTask extends SetupTask {

    public static final String[] moduleNames = new String[] { "gravia-provision", "gravia-resolver", "gravia-repository" };

    protected EmbeddedTestSupport getEmbeddedTestSupport() {
        return new EmbeddedTestSupport();
    }

    protected List<URL> getInitialModuleLocations() throws IOException {
        List<URL> modules = new ArrayList<>();
        for (String modname : moduleNames) {
            File modfile = Paths.get("target", "modules", modname + ".jar").toFile();
            modules.add(modfile.toURI().toURL());
        }
        return modules;
    }

    @Override
    protected void beforeClass(SetupContext context) throws Exception {
        Runtime runtime = context.getSuiteStore().get(Runtime.class);
        if (runtime == null) {

            // Install and start the bootstrap modules
            EmbeddedTestSupport support = getEmbeddedTestSupport();
            runtime = support.getEmbeddedRuntime();
            for (URL url : getInitialModuleLocations()) {
                ClassLoader classLoader = EmbeddedTestSupport.class.getClassLoader();
                support.installAndStartModule(classLoader, url);
            }

            context.getSuiteStore().add(Runtime.class, runtime);
        }
    }

    @Override
    protected void afterSuite(SetupContext context) throws Exception {
        Runtime runtime = context.getSuiteStore().get(Runtime.class);
        if (runtime != null && !runtime.shutdownComplete()) {
            Assert.assertTrue(runtime.shutdown().awaitShutdown(20, TimeUnit.SECONDS));
            RuntimeLocator.releaseRuntime();
        }
    }
}
