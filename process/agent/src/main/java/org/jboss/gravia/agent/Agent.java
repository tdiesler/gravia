/*
 * #%L
 * Gravia :: Agent
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

package org.jboss.gravia.agent;

import java.io.InputStream;
import java.util.Dictionary;
import java.util.concurrent.TimeUnit;

import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.embedded.spi.EmbeddedRuntimeFactory;
import org.jboss.gravia.runtime.spi.DefaultPropertiesProvider;
import org.jboss.gravia.runtime.spi.PropertiesHeadersProvider;
import org.jboss.gravia.runtime.spi.PropertiesProvider;


/**
 * The gravia agent process.
 *
 * @author thomas.diesler@jboss.com
 * @since 29-May-2014
 */
public final class Agent {

    private Runtime runtime;

    public static void main(String[] args) throws Exception {
        Agent agent = new Agent();
        agent.start();
    }

    public void start() throws Exception {

        // Create Runtime
        PropertiesProvider propsProvider = new DefaultPropertiesProvider();
        runtime = RuntimeLocator.createRuntime(new EmbeddedRuntimeFactory(), propsProvider);
        runtime.init();

        // Install/Start the Agent as a Module
        Module module = installAgentModule();
        module.start();
    }

    private Module installAgentModule() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream input = classLoader.getResourceAsStream("META-INF/agent-module.headers");
        Dictionary<String, String> headers = new PropertiesHeadersProvider(input).getHeaders();
        return runtime.installModule(classLoader, headers);
    }

    public boolean shutdown(long timeout, TimeUnit unit) {
        runtime.shutdown();
        try {
            return runtime.awaitShutdown(timeout, unit);
        } catch (InterruptedException ex) {
            return false;
        }
    }
}
